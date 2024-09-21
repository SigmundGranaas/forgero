package com.sigmundgranaas.forgero.feature.onhit.entity;

import static com.sigmundgranaas.forgero.handler.HandlerBuilder.buildHandlerFromJson;

import java.util.List;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateFeature;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.FeatureBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.handler.afterUse.AfterUseHandler;
import com.sigmundgranaas.forgero.handler.targeted.onHitEntity.EntityTargetHandler;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * <p>The OnHitEntityFeature class extends BasePredicateFeature and implements OnHitHandler and AfterUseHandler interfaces.
 * It provides a framework for defining actions that occur when an entity is hit by an item and actions after the item's use.</p>
 *
 * <p><b>JSON Configuration Format:</b>
 * The JSON configuration for this feature enables specification of handlers for on-hit events and post-use actions.
 * This allows mod developers to create unique interactions and effects based on item usage.</p>
 *
 * <h3>JSON Configuration Example:</h3>
 * <p>This example configuration is designed for an item that causes an explosion upon hitting an entity and then consumes the entire item stack after use.</p>
 * <pre>
 * {
 *   "type": "minecraft:on_hit",
 *   "on_hit": {
 *     "type": "minecraft:explosion",
 *     "target": "minecraft:targeted_entity",
 *     "power": 10
 *   },
 *   "after_use": [
 *     {
 *       "type": "minecraft:consume_stack"
 *     }
 *   ]
 * }
 * </pre>
 *
 * <p>In this configuration:</p>
 * <ul>
 *   <li><b>on_hit:</b> Defines an explosion effect targeting the hit entity with a specified power level.</li>
 *   <li><b>after_use:</b> Specifies that the item stack should be consumed after the item is used.</li>
 * </ul>
 *
 * <p>This configuration will result in the item triggering a powerful explosion when it hits an entity, followed by the item being consumed completely from the player's inventory.</p>
 */
public class OnHitEntityFeature extends BasePredicateFeature implements EntityTargetHandler, AfterUseHandler {
	public static final String ON_HIT_TYPE = "minecraft:on_hit";
	public static final ClassKey<OnHitEntityFeature> KEY = new ClassKey<>(ON_HIT_TYPE, OnHitEntityFeature.class);
	public static final String ON_HIT = "on_hit";

	public static final FeatureBuilder<OnHitEntityFeature> BUILDER = FeatureBuilder.of(ON_HIT_TYPE, OnHitEntityFeature::buildFromBase);
	private final List<EntityTargetHandler> handler;
	private final List<AfterUseHandler> afterUseHandlers;

	public OnHitEntityFeature(BasePredicateData data, List<EntityTargetHandler> handler, List<AfterUseHandler> afterUseHandlers) {
		super(data);
		this.handler = handler;
		this.afterUseHandlers = afterUseHandlers;
		if (!data.type().equals(ON_HIT_TYPE)) {
			throw new IllegalArgumentException("Type needs to be: " + ON_HIT_TYPE);
		}
	}

	private static OnHitEntityFeature buildFromBase(BasePredicateData data, JsonElement element) {
		List<EntityTargetHandler> handler = buildHandlerFromJson(element, ON_HIT, obj -> HandlerBuilder.DEFAULT.build(EntityTargetHandler.KEY, obj));
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
