package com.sigmundgranaas.forgero.predicate.minecraft.standalone;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.EvaluableCondition;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;

/**
 * Dynamic condition: passes when the number of living entities within {@code radius} of the source
 * is within [{@code min}, {@code max}]. Enables crowd-scaling mechanics ("stronger when surrounded").
 *
 * <pre>{ "type": "forgero:crowd_count", "radius": 5, "min": 3 }</pre>
 */
public record CrowdCountPredicate(double radius, int min, Optional<Integer> max) implements EvaluableCondition {
	public static final OpenIdentifier TYPE = new OpenIdentifier("forgero", "crowd_count");

	public static final Codec<CrowdCountPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.DOUBLE.fieldOf("radius").forGetter(CrowdCountPredicate::radius),
			Codec.INT.optionalFieldOf("min", 1).forGetter(CrowdCountPredicate::min),
			Codec.INT.optionalFieldOf("max").forGetter(CrowdCountPredicate::max)
	).apply(instance, CrowdCountPredicate::new));

	public CrowdCountPredicate {
		if (radius <= 0) {
			throw new IllegalArgumentException("radius must be > 0, got: " + radius);
		}
		if (min < 0) {
			throw new IllegalArgumentException("min must be >= 0, got: " + min);
		}
	}

	@Override
	public boolean test(DynamicContext context) {
		Optional<Entity> anchorOpt = context.get(MinecraftContextKeys.SOURCE_ENTITY);
		if (anchorOpt.isEmpty()) {
			return false;
		}
		Entity anchor = anchorOpt.get();
		double radiusSquared = radius * radius;
		Box box = new Box(anchor.getBlockPos()).expand(radius);
		int count = anchor.getWorld().getOtherEntities(anchor, box,
				e -> e instanceof LivingEntity && e.squaredDistanceTo(anchor) <= radiusSquared).size();
		return count >= min && max.map(m -> count <= m).orElse(true);
	}

	@Override
	public OpenIdentifier type() {
		return TYPE;
	}
}
