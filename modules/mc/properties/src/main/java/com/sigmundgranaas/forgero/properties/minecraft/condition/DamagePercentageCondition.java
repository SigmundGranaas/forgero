package com.sigmundgranaas.forgero.properties.minecraft.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import net.minecraft.item.ItemStack;

/**
 * A dynamic condition that checks if an item's damage percentage exceeds a threshold.
 * Useful for durability-based effects or abilities that trigger when an item is heavily damaged.
 *
 * <h3>Example (percentage as 0-1):</h3>
 * <pre>
 * {
 *   "type": "forgero:damage_percentage",
 *   "percentage": 0.5
 * }
 * </pre>
 *
 * <h3>Example (percentage as 0-100):</h3>
 * <pre>
 * {
 *   "type": "forgero:damage_percentage",
 *   "percentage": 75
 * }
 * </pre>
 *
 * <p>If percentage > 1, it's treated as a 0-100 scale, otherwise 0-1.</p>
 */
public record DamagePercentageCondition(OpenIdentifier type, float percentage) implements DynamicCondition {
	public static final Codec<DamagePercentageCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(DamagePercentageCondition::type),
					Codec.FLOAT.fieldOf("percentage").forGetter(DamagePercentageCondition::percentage)
			).apply(instance, DamagePercentageCondition::new));

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
}
