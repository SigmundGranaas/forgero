package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Filters entities by their specific entity type.
 * Example: "minecraft:zombie", "minecraft:skeleton", "minecraft:creeper"
 */
public record EntityTypeFilter(String entityType) implements EntityFilter {
	public static final String TYPE = "forgero:entity_type";
	public static final Codec<EntityTypeFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("entity_type").forGetter(EntityTypeFilter::entityType)
	).apply(instance, EntityTypeFilter::new));

	@Override
	public boolean test(Entity source, Entity candidate) {
		Identifier typeId = Identifier.tryParse(entityType);
		if (typeId == null) {
			return false;
		}

		EntityType<?> targetType = Registries.ENTITY_TYPE.get(typeId);
		return candidate.getType().equals(targetType);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
