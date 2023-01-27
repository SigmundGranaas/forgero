package com.sigmundgranaas.forgero.minecraft.common.entity;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.soul.Soul;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.SoulEncoder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static com.sigmundgranaas.forgero.minecraft.common.entity.Entities.SOUL_ENTITY;
import static com.sigmundgranaas.forgero.minecraft.common.item.Items.BOTTLED_SOUL;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.SOUL_IDENTIFIER;
import static net.minecraft.item.Items.GLASS_BOTTLE;

public class SoulEntity extends PathAwareEntity {
    public static final Identifier SOUL_ENTITIY_IDENTIFIER = new Identifier(Forgero.NAMESPACE, "soul_entity");
    private final Soul soul;

    public SoulEntity(World world, Soul soul, double x, double y, double z) {
        super(SOUL_ENTITY, world);
        this.setPosition(x, y, z);
        this.soul = soul;
    }

    public SoulEntity(World world, Soul soul) {
        super(SOUL_ENTITY, world);
        this.soul = soul;
    }

    public SoulEntity(EntityType<? extends SoulEntity> type, World world) {
        super(type, world);
        this.soul = new Soul();
    }

    public Soul getSoul() {
        return soul;
    }

    public void playSoulSound() {
        this.playSound(SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_DEATH, getSoundVolume(), getSoundPitch());
    }

    @Override
    public void playSpawnEffects() {
        playSoulSound();
        createSoulParticles(30);
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIFIED_PIGLIN_DEATH;
    }

    public void createSoulParticles(int amount) {
        for (int i = 0; i < amount; i++) {
            this.world.addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX() + this.random.nextGaussian() / 3, this.getY() + this.random.nextGaussian() / 3, this.getZ() + this.random.nextGaussian() / 3, 0, 0, 0);
        }
    }


    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(GLASS_BOTTLE)) {
            createSoulParticles(30);
            playSoulSound();
            if (this.world.isClient) {
                return ActionResult.CONSUME;
            }
            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }
            var soulCompound = new NbtCompound();
            soulCompound.put(SOUL_IDENTIFIER, SoulEncoder.ENCODER.encode(getSoul()));
            ItemStack soulStack = new ItemStack(BOTTLED_SOUL);
            soulStack.getOrCreateNbt().put(FORGERO_IDENTIFIER, soulCompound);
            player.giveItemStack(soulStack);
            this.remove(Entity.RemovalReason.DISCARDED);
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }
}
