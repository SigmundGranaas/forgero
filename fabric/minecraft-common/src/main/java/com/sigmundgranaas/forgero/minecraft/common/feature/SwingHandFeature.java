package com.sigmundgranaas.forgero.minecraft.common.feature;

import static com.sigmundgranaas.forgero.minecraft.common.handler.HandlerBuilder.buildHandlerFromJson;

import java.util.List;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateFeature;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.FeatureBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.AfterUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.swing.EntityHandHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;


/**
 * <pre>
 * {
 * "type": "minecraft:on_swing",
 * "swing": [
 * {
 * "type": "minecraft:play_sound",
 * "sound": "minecraft:entity.zombie.ambient"
 * }
 * ]
 * }
 * </pre>
 */
public class SwingHandFeature extends BasePredicateFeature implements EntityHandHandler, AfterUseHandler {
	public static final String ON_SWING_TYPE = "minecraft:on_swing";
	public static final ClassKey<SwingHandFeature> KEY = new ClassKey<>(ON_SWING_TYPE, SwingHandFeature.class);
	public static final String ON_SWING = "swing";

	public static final FeatureBuilder<SwingHandFeature> BUILDER = FeatureBuilder.of(ON_SWING_TYPE, SwingHandFeature::buildFromBase);
	private final List<EntityHandHandler> handler;
	private final List<AfterUseHandler> afterUseHandlers;

	public SwingHandFeature(BasePredicateData data, List<EntityHandHandler> handler, List<AfterUseHandler> afterUseHandlers) {
		super(data);
		this.handler = handler;
		this.afterUseHandlers = afterUseHandlers;
		if (!data.type().equals(ON_SWING_TYPE)) {
			throw new IllegalArgumentException("Type needs to be: " + ON_SWING_TYPE);
		}
	}

	private static SwingHandFeature buildFromBase(BasePredicateData data, JsonElement element) {
		List<EntityHandHandler> handler = buildHandlerFromJson(element, ON_SWING, obj -> HandlerBuilder.DEFAULT.build(EntityHandHandler.KEY, obj));
		List<AfterUseHandler> afterUseHandler = buildHandlerFromJson(element, AFTER_USE, obj -> HandlerBuilder.DEFAULT.build(AfterUseHandler.KEY, obj));

		return new SwingHandFeature(data, handler, afterUseHandler);
	}

	@Override
	public String type() {
		return ON_SWING_TYPE;
	}


	@Override
	public void handle(Entity source, ItemStack target, Hand hand) {
		afterUseHandlers.forEach(sub -> sub.handle(source, target, hand));
	}

	@Override
	public void onSwing(Entity source, Hand hand) {
		handler.forEach(sub -> sub.onSwing(source, hand));
		if (source instanceof PlayerEntity playerEntity) {
			handle(source, playerEntity.getStackInHand(hand), hand);
		}
	}
}
