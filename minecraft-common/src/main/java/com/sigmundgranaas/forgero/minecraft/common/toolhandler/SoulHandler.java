package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.soul.Soul;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.SoulEncoder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.SoulParser;
import com.sigmundgranaas.forgero.minecraft.common.mixins.OreBlockXp;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.Random;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.SOUL_IDENTIFIER;

public class SoulHandler {
    private final ItemStack stack;

    private final Soul soul;

    public SoulHandler(ItemStack stack, Soul soul) {
        this.stack = stack;
        this.soul = soul;
    }

    public static Optional<SoulHandler> of(ItemStack stack) {
        return SoulParser.of(stack).map(value -> new SoulHandler(stack, value));
    }

    public void processMobKill(Entity entity, World world, PlayerEntity player) {
        var handledSoul = soul.addXp(100);
        if (handledSoul.getLevel() > soul.getLevel()) {
            handleLevelUp(handledSoul, world, player);
        }
        handledSoul.trackMob(Registry.ENTITY_TYPE.getId(entity.getType()).toString(), 1);
        stack.getOrCreateNbt().getCompound(FORGERO_IDENTIFIER).put(SOUL_IDENTIFIER, SoulEncoder.ENCODER.encode(handledSoul));
    }


    public void processBlockBreak(BlockState state, BlockPos pos, World world, PlayerEntity player) {
        int xp = (int) (state.getHardness(world, pos) * 1);
        if (state.getBlock() instanceof OreBlockXp ore) {
            xp = ore.getExperienceDropped().get(net.minecraft.util.math.random.Random.create()) * 15;
        }
        var handledSoul = soul.addXp(xp);
        if (handledSoul.getLevel() > soul.getLevel()) {
            handleLevelUp(handledSoul, world, player);
        }
        handledSoul.trackBlock(Registry.BLOCK.getId(state.getBlock()).toString(), 1);
        stack.getOrCreateNbt().getCompound(FORGERO_IDENTIFIER).put(SOUL_IDENTIFIER, SoulEncoder.ENCODER.encode(handledSoul));
    }

    private void handleLevelUp(Soul soul, World world, PlayerEntity player) {
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            world.addParticle(ParticleTypes.FIREWORK, rand.nextDouble(), rand.nextDouble(), rand.nextDouble(), 0, 0, 0);
        }
        world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
    }
}
