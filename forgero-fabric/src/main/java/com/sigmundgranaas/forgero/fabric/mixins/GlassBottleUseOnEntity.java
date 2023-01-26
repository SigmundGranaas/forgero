package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.SoulEncoder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GlassBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;

import static com.sigmundgranaas.forgero.fabric.ForgeroInitializer.BOTTLED_SOUL;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.SOUL_IDENTIFIER;

@Mixin(GlassBottleItem.class)
public abstract class GlassBottleUseOnEntity extends Item {

    public GlassBottleUseOnEntity(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity instanceof SoulEntity soul) {
            entity.remove(Entity.RemovalReason.DISCARDED);
            ItemStack soulStack = new ItemStack(BOTTLED_SOUL);
            var soulCompound = new NbtCompound();
            soulCompound.put(SOUL_IDENTIFIER, SoulEncoder.ENCODER.encode(soul.getSoul()));
            soulStack.getOrCreateNbt().put(FORGERO_IDENTIFIER, soulCompound);
            entity.getStackInHand(hand).decrement(1);
            user.giveItemStack(soulStack);
            return ActionResult.SUCCESS;
        }
        return super.useOnEntity(stack, user, entity, hand);
    }
}
