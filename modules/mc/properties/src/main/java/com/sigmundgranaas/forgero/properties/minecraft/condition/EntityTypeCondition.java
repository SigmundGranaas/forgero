package com.sigmundgranaas.forgero.properties.minecraft.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * A dynamic condition that checks if an entity matches a specific type.
 * Checks both source and target entities from the dynamic context.
 */
public record EntityTypeCondition(OpenIdentifier type, Identifier entityType) implements DynamicCondition {
	public static final Codec<EntityTypeCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(EntityTypeCondition::type),
					Identifier.CODEC.fieldOf("entity_type").forGetter(EntityTypeCondition::entityType)
			).apply(instance, EntityTypeCondition::new));

	@Override
	public boolean test(DynamicContext context) {
		// Check source entity
		boolean sourceMatches = context.get(MinecraftContextKeys.SOURCE_ENTITY)
				.map(this::matchesEntityType)
				.orElse(false);

		if (sourceMatches) {
			return true;
		}

		// Check target entity
		return context.get(MinecraftContextKeys.TARGET_ENTITY)
				.map(this::matchesEntityType)
				.orElse(false);
	}

	private boolean matchesEntityType(Entity entity) {
		EntityType<?> expectedType = Registries.ENTITY_TYPE.get(entityType);
		return entity.getType() == expectedType;
	}
}
