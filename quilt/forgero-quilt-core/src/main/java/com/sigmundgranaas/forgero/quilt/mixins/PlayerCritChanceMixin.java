package com.sigmundgranaas.forgero.quilt.mixins;

import com.sigmundgranaas.forgero.core.property.v2.Attribute;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.CriticalHitChanceHandler;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.PropertyHelper;

import net.minecraft.entity.player.PlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Random;

@Mixin(PlayerEntity.class)
public abstract class PlayerCritChanceMixin {
	@ModifyVariable(method = "attack", at = @At(value = "STORE"), ordinal = 2)
	private boolean injected(boolean critical) {
		if (critical) {
			return true;
		}
		Random rand = new Random();
		return PropertyHelper.ofPlayerHands((PlayerEntity) (Object) this)
				.flatMap(CriticalHitChanceHandler::of)
				.map(Attribute::asFloat)
				.orElse(0f) > rand.nextFloat();
	}
}


