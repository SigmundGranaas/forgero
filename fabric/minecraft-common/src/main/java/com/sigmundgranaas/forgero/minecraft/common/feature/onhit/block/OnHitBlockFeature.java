package com.sigmundgranaas.forgero.minecraft.common.feature.onhit.block;

import static com.sigmundgranaas.forgero.minecraft.common.handler.HandlerBuilder.buildHandlerFromJson;

import java.util.List;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateFeature;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.FeatureBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.AfterUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.targeted.onHitBlock.BlockTargetHandler;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OnHitBlockFeature extends BasePredicateFeature implements BlockTargetHandler, AfterUseHandler {
	public static final String ON_HIT_TYPE = "minecraft:on_hit_block";
	public static final ClassKey<OnHitBlockFeature> KEY = new ClassKey<>(ON_HIT_TYPE, OnHitBlockFeature.class);
	public static final String ON_HIT = "on_hit";
	public static final FeatureBuilder<OnHitBlockFeature> BUILDER = FeatureBuilder.of(ON_HIT_TYPE, OnHitBlockFeature::buildFromBase);
	private final List<BlockTargetHandler> handler;
	private final List<AfterUseHandler> afterUseHandlers;

	public OnHitBlockFeature(BasePredicateData data, List<BlockTargetHandler> handler, List<AfterUseHandler> afterUseHandlers) {
		super(data);
		this.handler = handler;
		this.afterUseHandlers = afterUseHandlers;
		if (!data.type().equals(ON_HIT_TYPE)) {
			throw new IllegalArgumentException("Type needs to be: " + ON_HIT_TYPE);
		}
	}

	private static OnHitBlockFeature buildFromBase(BasePredicateData data, JsonElement element) {
		List<BlockTargetHandler> handler = buildHandlerFromJson(element, ON_HIT, obj -> HandlerBuilder.DEFAULT.build(BlockTargetHandler.KEY, obj));
		List<AfterUseHandler> afterUseHandler = buildHandlerFromJson(element, AFTER_USE, obj -> HandlerBuilder.DEFAULT.build(AfterUseHandler.KEY, obj));

		return new OnHitBlockFeature(data, handler, afterUseHandler);
	}

	@Override
	public String type() {
		return ON_HIT_TYPE;
	}

	@Override
	public void onHit(Entity root, World world, BlockPos target) {
		handler.forEach(sub -> sub.onHit(root, world, target));
	}

	@Override
	public void handle(Entity source, ItemStack target, Hand hand) {
		afterUseHandlers.forEach(sub -> sub.handle(source, target, hand));
	}
}
