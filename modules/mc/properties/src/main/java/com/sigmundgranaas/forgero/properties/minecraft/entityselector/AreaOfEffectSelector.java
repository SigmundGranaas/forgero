package com.sigmundgranaas.forgero.properties.minecraft.entityselector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityFilter;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

import java.util.Collections;
import java.util.List;

/**
 * Selects all entities within a radius of the initial target.
 * Applies filters to narrow down the selection.
 */
public class AreaOfEffectSelector extends FilterableSelector {
	public static final String TYPE = "forgero:aoe";

	private final int radius;

	public static final Codec<AreaOfEffectSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("radius").forGetter(AreaOfEffectSelector::radius),
			Codec.list(EntityFilter.CODEC).optionalFieldOf("filters", Collections.emptyList()).forGetter(FilterableSelector::filters)
	).apply(instance, AreaOfEffectSelector::new));

	public AreaOfEffectSelector(int radius, List<EntityFilter> filters) {
		super(filters);
		if (radius <= 0) {
			throw new IllegalArgumentException("radius must be > 0, got: " + radius);
		}
		this.radius = radius;
	}

	public int radius() {
		return radius;
	}

	@Override
	protected List<Entity> selectEntities(Entity source, Entity initialTarget) {
		Box box = new Box(initialTarget.getBlockPos()).expand(radius);
		return initialTarget.getWorld().getOtherEntities(source, box);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
