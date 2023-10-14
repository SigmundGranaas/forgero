package com.sigmundgranaas.forgero.minecraft.common.feature;

import static com.sigmundgranaas.forgero.minecraft.common.handler.HandlerBuilder.buildHandlerFromJson;

import java.util.List;

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
	private final List<EntityHandler> handler;

	public EntityTickFeature(BasePredicateData data, List<EntityHandler> handler) {
		super(data);
		this.handler = handler;
		if (!data.type().equals(ENTITY_TICK_TYPE)) {
			throw new IllegalArgumentException("Type needs to be: " + ENTITY_TICK_TYPE);
		}
	}

	private static EntityTickFeature buildFromBase(BasePredicateData data, JsonElement element) {
		List<EntityHandler> handler = buildHandlerFromJson(element, HANDLER, obj -> HandlerBuilder.DEFAULT.build(EntityHandler.KEY, obj));
		return new EntityTickFeature(data, handler);
	}

	@Override
	public void handle(Entity entity) {
		handler.forEach(sub -> sub.handle(entity));
	}
}
