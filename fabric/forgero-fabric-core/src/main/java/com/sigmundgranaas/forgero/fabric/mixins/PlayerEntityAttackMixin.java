package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityAttackMixin {
	@ModifyVariable(method = "attack", at = @At("STORE"), ordinal = 0)
	private float injected(float x, Entity target) {
		if (((PlayerEntity) (Object) this).getMainHandStack().getItem() instanceof StateItem item) {
			var stack = ((PlayerEntity) (Object) this).getMainHandStack();
			var tool = item.dynamicState(stack);
			float initialAttackDamage = tool.stream().applyAttribute(AttackDamage.KEY);
			// Todo fix actual property here
			float attackDamageTarget = AttackDamage.apply(tool, MatchContext.of());
			if (initialAttackDamage != attackDamageTarget) {
				return x + attackDamageTarget - initialAttackDamage;
			}
		}
		return x;
	}
}


