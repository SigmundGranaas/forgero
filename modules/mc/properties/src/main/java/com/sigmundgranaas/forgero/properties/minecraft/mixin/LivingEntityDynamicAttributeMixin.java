package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.common.runtime.DynamicAttributes;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.common.runtime.DynamicContextFactory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Applies dynamic-conditional attack-damage attributes at the point damage is dealt.
 *
 * <p>This is the runtime application site for dynamic attributes: it has the attacker and
 * target in hand, so it can build a {@link DynamicContext} and evaluate conditions that the
 * compile-time layer deliberately left as data (e.g. a dagger's "+damage while sneaking").
 * Vanilla supplies the base attack damage via the item's attribute modifiers; this adds the
 * qualifying conditional bonus on top — the honest place for stat behaviour that depends on
 * live game state.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDynamicAttributeMixin {

	@ModifyVariable(method = "applyDamage", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private float forgero$applyDynamicAttackDamage(float amount, DamageSource source) {
		// Avoid recursion from explosion-based effects, matching the on-hit hook.
		if (source.isOf(DamageTypes.EXPLOSION) || source.isOf(DamageTypes.PLAYER_EXPLOSION)) {
			return amount;
		}
		Entity attacker = source.getAttacker();
		if (!(attacker instanceof LivingEntity livingAttacker)) {
			return amount;
		}
		// Only direct melee from the attacker's weapon; projectiles/indirect sources are out.
		if (source.getSource() != attacker) {
			return amount;
		}
		ItemStack stack = livingAttacker.getMainHandStack();
		if (stack.isEmpty()) {
			return amount;
		}
		LivingEntity target = (LivingEntity) (Object) this;
		DynamicContext context = DynamicContextFactory.fromEntities(livingAttacker, target);
		return amount + DynamicAttributes.bonus(stack, DefaultAttributes.ATTACK_DAMAGE, context);
	}
}
