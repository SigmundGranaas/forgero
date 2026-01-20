package com.sigmundgranaas.forgero.fabric.mixins;

import java.util.Random;

import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.CriticalHitChanceHandler;
import com.sigmundgranaas.forgero.minecraft.common.toolhandler.PropertyHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.entity.player.PlayerEntity;

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
				.map(ComputedAttribute::asFloat)
				.orElse(0f) > rand.nextFloat();
	}
}


