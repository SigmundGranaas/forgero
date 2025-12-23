package com.sigmundgranaas.forgero.properties.minecraft.entityselector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.properties.minecraft.entityfilter.EntityFilter;
import net.minecraft.entity.Entity;

import java.util.Collections;
import java.util.List;

public class SingleTargetSelector extends FilterableSelector {
	public static final String TYPE = "forgero:single_target";

	public static final Codec<SingleTargetSelector> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.list(EntityFilter.CODEC).optionalFieldOf("filters", Collections.emptyList()).forGetter(FilterableSelector::filters)
	).apply(instance, SingleTargetSelector::new));

	public SingleTargetSelector(List<EntityFilter> filters) {
		super(filters);
	}

	@Override
	protected List<Entity> selectEntities(Entity source, Entity initialTarget) {
		return List.of(initialTarget);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
