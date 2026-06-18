package com.sigmundgranaas.forgero.properties.minecraft.entityselector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityFilter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.List;

/**
 * Selects all entities within a spherical radius of the initial target.
 * <p>
 * The {@code radius} is a true distance (in blocks) measured from the target's position, so
 * entities in the corners of the bounding box that fall outside the sphere are excluded.
 * Applies filters to narrow down the selection and optionally caps the result via {@code maxTargets}.
 */
public class AreaOfEffectSelector extends FilterableSelector {
	public static final String TYPE = "forgero:aoe";

	private final int radius;

	public static final Codec<AreaOfEffectSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("radius").forGetter(AreaOfEffectSelector::radius),
			Codec.list(EntityFilter.CODEC).optionalFieldOf("filters", Collections.emptyList()).forGetter(FilterableSelector::filters),
			FilterMode.CODEC.optionalFieldOf("match", FilterMode.ALL).forGetter(FilterableSelector::filterMode),
			Codec.INT.optionalFieldOf("maxTargets", UNLIMITED_TARGETS).forGetter(FilterableSelector::maxTargets)
	).apply(instance, AreaOfEffectSelector::new));

	public AreaOfEffectSelector(int radius, List<EntityFilter> filters) {
		this(radius, filters, FilterMode.ALL, UNLIMITED_TARGETS);
	}

	public AreaOfEffectSelector(int radius, List<EntityFilter> filters, FilterMode filterMode, int maxTargets) {
		super(filters, filterMode, maxTargets);
		if (radius <= 0) {
			throw new IllegalArgumentException("radius must be > 0, got: " + radius);
		}
		if (radius > MAX_RANGE) {
			throw new IllegalArgumentException("radius must be <= " + (int) MAX_RANGE + " to avoid pathological world queries, got: " + radius);
		}
		this.radius = radius;
	}

	public int radius() {
		return radius;
	}

	@Override
	protected List<Entity> selectEntities(Entity source, Entity initialTarget) {
		Box box = new Box(initialTarget.getBlockPos()).expand(radius);
		Vec3d center = initialTarget.getPos();
		double radiusSquared = (double) radius * radius;
		return initialTarget.getWorld().getOtherEntities(source, box).stream()
				.filter(entity -> entity.squaredDistanceTo(center) <= radiusSquared)
				.toList();
	}

	@Override
	public String type() {
		return TYPE;
	}
}
