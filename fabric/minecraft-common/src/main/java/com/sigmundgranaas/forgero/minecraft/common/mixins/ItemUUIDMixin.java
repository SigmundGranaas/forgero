package com.sigmundgranaas.forgero.minecraft.common.mixins;

import net.minecraft.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(Item.class)
public interface ItemUUIDMixin {
	@Accessor("ATTACK_DAMAGE_MODIFIER_ID")
	static UUID getAttackDamageModifierID() {
		throw new AssertionError();
	}

	@Accessor("ATTACK_SPEED_MODIFIER_ID")
	static UUID getAttackSpeedModifierID() {
		throw new AssertionError();
	}
}
