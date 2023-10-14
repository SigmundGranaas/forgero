package com.sigmundgranaas.forgero.minecraft.common.feature;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateFeature;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.FeatureBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.entity.EntityHandler;

import net.minecraft.entity.Entity;

public class EntityTickFeature extends BasePredicateFeature implements EntityHandler {
	public static final String ENTITY_TICK_TYPE = "minecraft:entity_tick";
	public static final ClassKey<EntityTickFeature> KEY = new ClassKey<>(ENTITY_TICK_TYPE, EntityTickFeature.class);
	public static final String HANDLER = "handler";
	public static final FeatureBuilder<EntityTickFeature> BUILDER = FeatureBuilder.of(ENTITY_TICK_TYPE, EntityTickFeature::buildFromBase);
	private final EntityHandler handler;

	public EntityTickFeature(BasePredicateData data, EntityHandler handler) {
		super(data);
		this.handler = handler;
		if (!data.type().equals(ENTITY_TICK_TYPE)) {
			throw new IllegalArgumentException("Type needs to be: " + ENTITY_TICK_TYPE);
		}
	}

	private static EntityTickFeature buildFromBase(BasePredicateData data, JsonElement element) {
		EntityHandler handler = DEFAULT;

		if (element.isJsonObject() && element.getAsJsonObject().has(HANDLER)) {
			var object = element.getAsJsonObject();
			var handlerOpt = HandlerBuilder.DEFAULT.build(EntityHandler.KEY, object.get(HANDLER));
			if (handlerOpt.isPresent()) {
				handler = handlerOpt.get();
			}
		}

		return new EntityTickFeature(data, handler);
	}

	@Override
	public void handle(Entity entity) {
		handler.handle(entity);
	}
}
