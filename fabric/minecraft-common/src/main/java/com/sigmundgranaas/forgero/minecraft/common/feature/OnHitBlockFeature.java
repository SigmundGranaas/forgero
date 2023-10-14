package com.sigmundgranaas.forgero.minecraft.common.feature;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateFeature;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.FeatureBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitBlock.OnHitBlockHandler;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OnHitBlockFeature extends BasePredicateFeature implements OnHitBlockHandler {
	public static final String ON_HIT_TYPE = "forgero:on_hit_block";
	public static final ClassKey<OnHitBlockFeature> KEY = new ClassKey<>(ON_HIT_TYPE, OnHitBlockFeature.class);
	public static final String ON_HIT = "on_hit";
	public static final FeatureBuilder<OnHitBlockFeature> BUILDER = FeatureBuilder.of(ON_HIT_TYPE, OnHitBlockFeature::buildFromBase);
	private final OnHitBlockHandler handler;

	public OnHitBlockFeature(BasePredicateData data, OnHitBlockHandler handler) {
		super(data);
		this.handler = handler;
		if (!data.type().equals(ON_HIT_TYPE)) {
			throw new IllegalArgumentException("Type needs to be: " + ON_HIT_TYPE);
		}
	}

	private static OnHitBlockFeature buildFromBase(BasePredicateData data, JsonElement element) {
		OnHitBlockHandler handler = DEFAULT;

		if (element.isJsonObject() && element.getAsJsonObject().has(ON_HIT)) {
			var object = element.getAsJsonObject();
			var handlerOpt = HandlerBuilder.DEFAULT.build(OnHitBlockHandler.KEY, object.get(ON_HIT));
			if (handlerOpt.isPresent()) {
				handler = handlerOpt.get();
			}
		}

		return new OnHitBlockFeature(data, handler);
	}

	@Override
	public String type() {
		return ON_HIT_TYPE;
	}

	@Override
	public void onHit(Entity root, World world, BlockPos target) {
		handler.onHit(root, world, target);
	}
}
