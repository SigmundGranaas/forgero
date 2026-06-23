package com.sigmundgranaas.forgero.properties.gametest;

import java.util.List;

import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.EvaluableCondition;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;
import com.sigmundgranaas.forgero.common.runtime.RuntimeConditions;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.effects.entity.FireHandler;
import com.sigmundgranaas.forgero.effects.entity.OnHitEffect;
import com.sigmundgranaas.forgero.properties.minecraft.EntityEffects;
import com.sigmundgranaas.forgero.properties.minecraft.entityselector.SingleTargetSelector;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitProperty;

import net.minecraft.entity.LivingEntity;

/**
 * Harness for proving a dynamic predicate actually GATES an effect in real gameplay.
 * <p>
 * It builds an on-hit fire effect gated by {@code predicate} and runs it through the EXACT runtime
 * gate the live game uses — {@link RuntimeConditions#filter} against a {@link DynamicContext} built
 * from the real (attacker, target, world), the same context {@code OnHitManager.handleOnHit} builds —
 * then applies the surviving effects via {@link EntityEffects} (mirroring the manager's loop) and
 * reports whether the fire landed. Call it twice (an arrangement where the predicate should pass and
 * one where it should fail) to assert a predicate gates a real effect on real entities in BOTH
 * branches, instead of only calling {@code predicate.test(fabricatedContext)} in isolation.
 * <p>
 * It feeds the property to the gate in-memory rather than serializing it onto an ItemStack, because
 * the {@link ComponentTester#createStack} test shortcut drops dynamic conditions during its bake;
 * the live data pipeline preserves them. The gate + predicate + effect application are the real code.
 */
public final class EffectGatingTestHelper {

	private EffectGatingTestHelper() {
	}

	/**
	 * Returns true if a {@code predicate}-gated fire effect lands on {@code target} when attacked by
	 * {@code attacker} — i.e. the predicate passes against the live context and the effect is applied.
	 */
	public static boolean gatedFireFires(LivingEntity attacker, LivingEntity target, EvaluableCondition predicate) {
		OnHitProperty gated = new OnHitProperty(
				new SingleTargetSelector(List.of()),
				List.<OnHitEffect>of(new FireHandler(5)),
				Condition.ofDynamic(predicate));

		DynamicContext context = new DynamicContext.Builder()
				.put(MinecraftContextKeys.SOURCE_ENTITY, attacker)
				.put(MinecraftContextKeys.TARGET_ENTITY, target)
				.put(MinecraftContextKeys.WORLD, attacker.getWorld())
				.build();

		target.setFireTicks(0); // clean slate so isOnFire() reflects only this dispatch

		// The exact gate the game uses: keep only properties whose dynamic conditions pass.
		List<OnHitProperty> active = RuntimeConditions.filter(List.of(gated), context);
		for (OnHitProperty property : active) {
			EntityEffects.apply(property.selector(), property.effects(), attacker, target);
		}

		return target.isOnFire();
	}
}
