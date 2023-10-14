package com.sigmundgranaas.forgero.minecraft.common.feature;

import java.util.ArrayList;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateFeature;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.FeatureBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.MultiOnHitHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.OnHitHandler;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class OnHitEntityFeature extends BasePredicateFeature implements OnHitHandler {
	public static final String ON_HIT_TYPE = "forgero:on_hit";
	public static final ClassKey<OnHitEntityFeature> KEY = new ClassKey<>(ON_HIT_TYPE, OnHitEntityFeature.class);
	public static final String ON_HIT = "on_hit";
	public static final FeatureBuilder<OnHitEntityFeature> BUILDER = FeatureBuilder.of(ON_HIT_TYPE, OnHitEntityFeature::buildFromBase);
	private final OnHitHandler handler;

	public OnHitEntityFeature(BasePredicateData data, OnHitHandler handler) {
		super(data);
		this.handler = handler;
		if (!data.type().equals(ON_HIT_TYPE)) {
			throw new IllegalArgumentException("Type needs to be: " + ON_HIT_TYPE);
		}
	}

	private static OnHitEntityFeature buildFromBase(BasePredicateData data, JsonElement element) {
		OnHitHandler handler = DEFAULT;

		if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.has(ON_HIT) && object.get(ON_HIT).isJsonObject()) {
				Optional<OnHitHandler> handlerOpt = tryBuild(object.get(ON_HIT).getAsJsonObject());
				if (handlerOpt.isPresent()) {
					handler = handlerOpt.get();
				}
			} else if (object.has(ON_HIT) && object.get(ON_HIT).isJsonArray()) {
				JsonArray array = object.get(ON_HIT).getAsJsonArray();
				ArrayList<OnHitHandler> handlers = new ArrayList<>();
				for (int i = 0; i < array.size(); i++) {
					JsonElement entry = array.get(i);
					if (entry.isJsonObject()) {
						tryBuild(entry.getAsJsonObject()).ifPresent(handlers::add);
					}
					handler = new MultiOnHitHandler(handlers);
				}
			}
		}


		return new OnHitEntityFeature(data, handler);
	}

	private static Optional<OnHitHandler> tryBuild(JsonObject element) {
		return HandlerBuilder.DEFAULT.build(OnHitHandler.KEY, element);
	}

	@Override
	public String type() {
		return ON_HIT_TYPE;
	}

	@Override
	public void onHit(Entity root, World world, Entity target) {
		handler.onHit(root, world, target);
	}
}
