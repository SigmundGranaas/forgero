package com.sigmundgranaas.forgero.minecraft.common.mixins;

import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(Item.class)
public interface ItemUUIDMixin {
    @Accessor
    UUID getATTACK_DAMAGE_MODIFIER_ID();
}
