package com.sigmundgranaas.forgero.fabric.mixins;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityAttackMixin {
	@Shadow
	public abstract boolean shouldDamagePlayer(PlayerEntity player);

	@ModifyVariable(method = "attack", at = @At("STORE"), ordinal = 0)
	private float injected(float x, Entity target) {
		PlayerEntity player = (PlayerEntity) (Object) this;
		ItemStack stack = player.getMainHandStack();
		if (stack.getItem() instanceof StateItem item) {
			var tool = item.dynamicState(stack);
			float initialAttackDamage = tool.stream().applyAttribute(AttackDamage.KEY);
			MatchContext context = new MatchContext()
					.put(ENTITY, player)
					.put(WORLD, player.getWorld())
					.put(STACK, stack)
					.put(ENTITY_TARGET, target);
			float attackDamageTarget = tool.stream().applyAttribute(MatchContext.DEFAULT_TRUE, context, AttackDamage.KEY);
			if (initialAttackDamage != attackDamageTarget) {
				return x + attackDamageTarget - initialAttackDamage;
			}
		}
		return x;
	}
}


