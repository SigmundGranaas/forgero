package com.sigmundgranaas.forgero.properties.minecraft.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * A dynamic condition that checks entity flags (state flags like sneaking, sprinting, etc.).
 * Supports multiple flag types and checks both source and target entities.
 */
public record EntityFlagCondition(OpenIdentifier type, EntityFlag flag) implements DynamicCondition {
	public static final Codec<EntityFlagCondition> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					CodecConstants.OPEN_IDENTIFIER_CODEC.fieldOf("type").forGetter(EntityFlagCondition::type),
					EntityFlag.CODEC.fieldOf("flag").forGetter(EntityFlagCondition::flag)
			).apply(instance, EntityFlagCondition::new));

	@Override
	public boolean test(DynamicContext context) {
		// Check source entity
		boolean sourceMatches = context.get(MinecraftContextKeys.SOURCE_ENTITY)
				.map(this::testFlag)
				.orElse(false);

		if (sourceMatches) {
			return true;
		}

		// Check target entity
		return context.get(MinecraftContextKeys.TARGET_ENTITY)
				.map(this::testFlag)
				.orElse(false);
	}

	private boolean testFlag(Entity entity) {
		return switch (flag) {
			case SNEAKING -> entity.isSneaking();
			case SPRINTING -> entity.isSprinting();
			case SWIMMING -> entity.isSwimming();
			case ON_GROUND -> entity.isOnGround();
			case USING -> entity instanceof PlayerEntity player && player.isUsingItem();
		};
	}

	/**
	 * Enum representing different entity state flags that can be checked.
	 */
	public enum EntityFlag {
		SNEAKING("sneaking"),
		SPRINTING("sprinting"),
		SWIMMING("swimming"),
		ON_GROUND("on_ground"),
		USING("using");

		private final String id;

		EntityFlag(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public static final Codec<EntityFlag> CODEC = Codec.STRING.xmap(
				id -> {
					for (EntityFlag flag : values()) {
						if (flag.getId().equals(id)) {
							return flag;
						}
					}
					throw new IllegalArgumentException("Unknown entity flag: " + id);
				},
				EntityFlag::getId
		);
	}
}
