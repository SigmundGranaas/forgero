package com.sigmundgranaas.forgero.minecraft.common.feature;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.v2.feature.*;
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.AfterUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitEntity.OnHitHandler;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;

import static com.sigmundgranaas.forgero.minecraft.common.handler.HandlerBuilder.buildHandlerFromJson;

public class OnHitEntityFeature extends BasePredicateFeature implements OnHitHandler, AfterUseHandler {
	public static final String ON_HIT_TYPE = "minecraft:on_hit";
	public static final ClassKey<OnHitEntityFeature> KEY = new ClassKey<>(ON_HIT_TYPE, OnHitEntityFeature.class);
	public static final String ON_HIT = "on_hit";

	public static final FeatureBuilder<OnHitEntityFeature> BUILDER = FeatureBuilder.of(ON_HIT_TYPE, OnHitEntityFeature::buildFromBase);
	private final List<OnHitHandler> handler;
	private final List<AfterUseHandler> afterUseHandlers;

	public OnHitEntityFeature(BasePredicateData data, List<OnHitHandler> handler, List<AfterUseHandler> afterUseHandlers) {
		super(data);
		this.handler = handler;
		this.afterUseHandlers = afterUseHandlers;
		if (!data.type().equals(ON_HIT_TYPE)) {
			throw new IllegalArgumentException("Type needs to be: " + ON_HIT_TYPE);
		}
	}

	private static OnHitEntityFeature buildFromBase(BasePredicateData data, JsonElement element) {
		List<OnHitHandler> handler = buildHandlerFromJson(element, ON_HIT, obj -> HandlerBuilder.DEFAULT.build(OnHitHandler.KEY, obj));
		List<AfterUseHandler> afterUseHandler = buildHandlerFromJson(element, AFTER_USE, obj -> HandlerBuilder.DEFAULT.build(AfterUseHandler.KEY, obj));

		return new OnHitEntityFeature(data, handler, afterUseHandler);
	}

	@Override
	public String type() {
		return ON_HIT_TYPE;
	}

	@Override
	public void onHit(Entity root, World world, Entity target) {
		handler.forEach(sub -> sub.onHit(root, world, target));
	}

	@Override
	public void handle(Entity source, ItemStack target, Hand hand) {
		afterUseHandlers.forEach(sub -> sub.handle(source, target, hand));
	}
}
