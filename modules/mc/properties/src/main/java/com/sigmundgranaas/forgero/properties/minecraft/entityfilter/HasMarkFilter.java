package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.effects.mark.MarkStore;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

/**
 * Selects only entities currently carrying a given mark. Enables targeting marked enemies
 * (e.g. an AOE that detonates everything marked).
 */
public record HasMarkFilter(Identifier mark) implements EntityFilter {
	public static final String TYPE = "forgero:has_mark";

	public static final Codec<HasMarkFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("mark").forGetter(HasMarkFilter::mark)
	).apply(instance, HasMarkFilter::new));

	@Override
	public boolean test(Entity source, Entity candidate) {
		return candidate instanceof LivingEntity living && MarkStore.hasMark(living, mark);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
