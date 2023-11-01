package com.sigmundgranaas.forgero.minecraft.common.feature;

import static com.sigmundgranaas.forgero.minecraft.common.handler.HandlerBuilder.buildHandlerFromJson;

import java.util.List;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateFeature;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.FeatureBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.OnHitHandler;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class OnHitEntityFeature extends BasePredicateFeature implements OnHitHandler {
	public static final String ON_HIT_TYPE = "minecraft:on_hit";
	public static final ClassKey<OnHitEntityFeature> KEY = new ClassKey<>(ON_HIT_TYPE, OnHitEntityFeature.class);
	public static final String ON_HIT = "on_hit";
	public static final FeatureBuilder<OnHitEntityFeature> BUILDER = FeatureBuilder.of(ON_HIT_TYPE, OnHitEntityFeature::buildFromBase);
	private final List<OnHitHandler> handler;

	public OnHitEntityFeature(BasePredicateData data, List<OnHitHandler> handler) {
		super(data);
		this.handler = handler;
		if (!data.type().equals(ON_HIT_TYPE)) {
			throw new IllegalArgumentException("Type needs to be: " + ON_HIT_TYPE);
		}
	}

	private static OnHitEntityFeature buildFromBase(BasePredicateData data, JsonElement element) {
		List<OnHitHandler> handler = buildHandlerFromJson(element, ON_HIT, obj -> HandlerBuilder.DEFAULT.build(OnHitHandler.KEY, obj));
		return new OnHitEntityFeature(data, handler);
	}

	@Override
	public String type() {
		return ON_HIT_TYPE;
	}

	@Override
	public void onHit(Entity root, World world, Entity target) {
		handler.forEach(sub -> sub.onHit(root, world, target));
	}
}
