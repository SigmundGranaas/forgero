package com.sigmundgranaas.forgero.predicate.minecraft.standalone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.predicate.minecraft.MinecraftContextKeys;

import net.minecraft.item.ItemStack;

/**
 * A dynamic condition that checks if an item's damage percentage exceeds a threshold.
 * Useful for durability-based effects or abilities that trigger when an item is heavily damaged.
 *
 * <h3>Example (percentage as 0-1):</h3>
 * <pre>
 * {
 *   "type": "forgero:damage",
 *   "percentage": 0.5
 * }
 * </pre>
 *
 * <h3>Example (percentage as 0-100):</h3>
 * <pre>
 * {
 *   "type": "forgero:damage",
 *   "percentage": 75
 * }
 * </pre>
 *
 * <p>If percentage > 1, it's treated as a 0-100 scale, otherwise 0-1.</p>
 */
public record DamagePredicate(float percentage) implements DynamicCondition {

	public static final OpenIdentifier TYPE = new OpenIdentifier("forgero", "damage");

	public static final Codec<DamagePredicate> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.FLOAT.fieldOf("percentage").forGetter(DamagePredicate::percentage)
			).apply(instance, DamagePredicate::new));

	@Override
	public boolean test(DynamicContext context) {
		return context.get(MinecraftContextKeys.STACK)
				.map(this::testStack)
				.orElse(false);
	}

	private boolean testStack(ItemStack stack) {
		if (!stack.isDamageable()) {
			return false;
		}

		float damageRatio = (float) stack.getDamage() / (float) stack.getMaxDamage();

		if (percentage > 1) {
			// Treat as 0-100 scale
			return (damageRatio * 100) >= percentage;
		} else {
			// Treat as 0-1 scale
			return damageRatio >= percentage;
		}
	}

	@Override
	public OpenIdentifier type() {
		return TYPE;
	}
}
