package com.sigmundgranaas.forgero.feature;

import static com.sigmundgranaas.forgero.handler.HandlerBuilder.buildHandlerFromJson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateData;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateFeature;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.FeatureBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.handler.afterUse.AfterUseHandler;
import com.sigmundgranaas.forgero.handler.use.BaseHandler;
import com.sigmundgranaas.forgero.handler.use.BlockUseHandler;
import com.sigmundgranaas.forgero.handler.use.EntityUseHandler;
import com.sigmundgranaas.forgero.handler.use.StopHandler;
import com.sigmundgranaas.forgero.handler.use.UseHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

/**
 * <p>The OnUseFeature class extends the BasePredicateFeature and implements various handlers
 * to manage actions when an item is used on entities, blocks, or just in the air. This feature
 * supports a variety of use scenarios including use on blocks, entities, after use, and when stopping
 * the use action. Custom titles and conditions can also be applied.</p>
 *
 * <p><b>JSON Configuration Format:</b>
 * The JSON configuration for this feature allows specifying different handlers for various use cases.
 * This includes defining actions for entity use, block use, after use, and on stopping use. Each action
 * can be customized using valid handlers. An example configuration is provided for illustration.</p>
 *
 * <h3>JSON Configuration Example:</h3>
 * <p>This particular configuration is designed for an item that behaves like a spear.
 * The configuration parameters include maximum use time, the action to be taken when used, and handlers for different stages of item usage, such as use, after use, and stopping use.</p>
 * <pre>
 * {
 *   "type": "minecraft:on_use",
 *   "max_use_time": 72000,
 *   "use_action": "SPEAR",
 *   "use": [
 *     {
 *       "type": "minecraft:consume"
 *     }
 *   ],
 *   "after_use": [
 *     {
 *       "type": "minecraft:stack_damage",
 *       "damage": 1
 *     }
 *   ],
 *   "on_stop": [
 *     {
 *       "type": "forgero:throw_trident"
 *     },
 *     {
 *       "type": "minecraft:play_sound",
 *       "sound": "minecraft:item.trident.throw"
 *     },
 *     {
 *       "type": "minecraft:consume_stack",
 *       "count": 1
 *     }
 *   ]
 * }
 * </pre>
 *
 * <p>This configuration will make the tool suffer one damage after the first use action, then upon stopping
 * the use action, it will throw a trident, play a sound, and decrement the item stack by one.</p>
 *
 * <h3>Configuration Details:</h3>
 * <ul>
 *   <li><b>max_use_time:</b> Sets the maximum duration the item can be used continuously.</li>
 *   <li><b>use_action:</b> Defines the action type, here set as 'SPEAR'.</li>
 *   <li><b>use:</b> Defines what happens when the item is used. In this case, the item is consumed.</li>
 *   <li><b>after_use:</b> Actions that occur after the item is used. Here, it damages the item stack by 1.</li>
 *   <li><b>on_stop:</b> Defines a series of actions that occur when the use of the item is stopped. This includes throwing a trident, playing a sound effect, and consuming a stack of the item.</li>
 * </ul>
 */
public class OnUseFeature extends BasePredicateFeature implements BlockUseHandler, EntityUseHandler, UseHandler, AfterUseHandler, StopHandler {
	public static final String TYPE = "minecraft:on_use";
	public static final ClassKey<OnUseFeature> KEY = new ClassKey<>(TYPE, OnUseFeature.class);
	public static final String USE = "use";
	public static final String ENTIY_USE = "entity";
	public static final String BLOCK_USE = "block";
	public static final String STOP_USE = "on_stop";
	public static final String AFTER_USE = "after_use";

	public static final FeatureBuilder<OnUseFeature> BUILDER = FeatureBuilder.of(TYPE, OnUseFeature::buildFromBase);

	private final List<UseHandler> onUse;

	private final List<EntityUseHandler> useOnEntity;

	private final List<BlockUseHandler> useOnBlock;

	private final List<StopHandler> onStoppedUsing;

	private final List<AfterUseHandler> afterUseHandlers;

	private final BaseHandler baseHandler;

	private final boolean usedOnRelease;

	private final UseAction action;

	private final int maxUseTime;

	public OnUseFeature(BasePredicateData data, List<UseHandler> onUse, List<EntityUseHandler> useOnEntity, List<BlockUseHandler> useOnBlock, List<StopHandler> onStoppedUsing, List<AfterUseHandler> afterUseHandlers, BaseHandler baseHandler, boolean usedOnRelease, UseAction action, int maxUseTime) {
		super(data);
		this.onUse = onUse;
		this.useOnEntity = useOnEntity;
		this.useOnBlock = useOnBlock;
		this.onStoppedUsing = onStoppedUsing;
		this.afterUseHandlers = afterUseHandlers;
		this.baseHandler = baseHandler;
		this.usedOnRelease = usedOnRelease;
		this.action = action;
		this.maxUseTime = maxUseTime;

		if (!data.type().equals(TYPE)) {
			throw new IllegalArgumentException("Type needs to be: " + TYPE);
		}
	}


	private static OnUseFeature buildFromBase(BasePredicateData data, JsonElement element) {
		List<UseHandler> use = parseHandler(UseHandler.KEY, element, USE);

		List<EntityUseHandler> entity = parseHandler(EntityUseHandler.KEY, element, ENTIY_USE);

		List<BlockUseHandler> block = parseHandler(BlockUseHandler.KEY, element, BLOCK_USE);

		List<StopHandler> stop = parseHandler(StopHandler.KEY, element, STOP_USE);

		List<AfterUseHandler> afterUseHandler = buildHandlerFromJson(element, AFTER_USE, obj -> HandlerBuilder.DEFAULT.build(AfterUseHandler.KEY, obj));

		int maxUseTime = 0;
		UseAction action = UseAction.NONE;

		if (element.isJsonObject()) {
			var object = element.getAsJsonObject();
			if (object.has("max_use_time")) {
				maxUseTime = object.get("max_use_time").getAsInt();
			}
			if (object.has("use_action")) {
				action = UseAction.valueOf(object.get("use_action").getAsString());
			}
		}

		BaseHandler base = Stream.of(use, entity, block, stop)
				.flatMap(List::stream)
				.map(BaseHandler.class::cast)
				.findFirst()
				.orElse(BaseHandler.DEFAULT);

		boolean usedOnRelease = element.isJsonObject() && element.getAsJsonObject().has("used_on_release") && element.getAsJsonObject().get("used_on_release").getAsBoolean();

		return new OnUseFeature(data, use, entity, block, stop, afterUseHandler, base, usedOnRelease, action, maxUseTime);
	}

	private static <T> List<T> parseHandler(ClassKey<T> key, JsonElement element, String jsonKey) {
		if (element.isJsonObject() && element.getAsJsonObject().has(jsonKey)) {
			var root = element.getAsJsonObject();
			if (root.get(jsonKey).isJsonObject() && element.getAsJsonObject().has(jsonKey)) {
				var handlerOpt = HandlerBuilder.DEFAULT.build(key, root.get(jsonKey));
				return handlerOpt.map(List::of).orElse(Collections.emptyList());
			} else if (root.get(jsonKey).isJsonArray()) {
				var elements = root.get(jsonKey).getAsJsonArray();
				var handlers = new ArrayList<T>();
				for (JsonElement jsonElement : elements) {
					if (jsonElement.isJsonObject()) {
						HandlerBuilder.DEFAULT.build(key, jsonElement.getAsJsonObject())
								.ifPresent(handlers::add);
					}
				}
				if (!handlers.isEmpty()) {
					return handlers;
				}
			}
		}
		return Collections.emptyList();
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (useOnBlock.isEmpty()) {
			return ActionResult.PASS;
		}
		ActionResult finalResult = ActionResult.PASS;
		for (BlockUseHandler handler : useOnBlock) {
			var result = handler.useOnBlock(context);
			if (result == ActionResult.FAIL) {
				finalResult = result;
				break;
			} else if (result.ordinal() < finalResult.ordinal()) {
				finalResult = result;
			}
		}
		if (finalResult != ActionResult.PASS) {
			afterUseHandlers.forEach(sub -> sub.handle(context.getPlayer(), context.getStack(), context.getHand()));
		}
		return finalResult;
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		if (useOnEntity.isEmpty()) {
			return ActionResult.PASS;
		}
		ActionResult finalResult = ActionResult.PASS;
		for (EntityUseHandler handler : useOnEntity) {
			var result = handler.useOnEntity(stack, user, entity, hand);
			if (result == ActionResult.FAIL) {
				finalResult = result;
				break;
			} else if (result.ordinal() < finalResult.ordinal()) {
				finalResult = result;
			}
		}
		if (finalResult != ActionResult.PASS) {
			afterUseHandlers.forEach(sub -> sub.handle(user, stack, hand));
		}
		return finalResult;
	}

	@Override
	public TypedActionResult<ItemStack> onUse(World world, PlayerEntity user, Hand hand) {
		if (onUse.isEmpty()) {
			return TypedActionResult.pass(user.getStackInHand(hand));
		}
		TypedActionResult<ItemStack> finalResult = TypedActionResult.pass(user.getStackInHand(hand));
		for (com.sigmundgranaas.forgero.handler.use.UseHandler handler : onUse) {
			TypedActionResult<ItemStack> result = handler.onUse(world, user, hand);
			if (result.getResult() == ActionResult.FAIL) {
				finalResult = result;
				break;
			} else if (result.getResult().ordinal() < finalResult.getResult().ordinal()) {
				finalResult = result;
			}
		}

		if (finalResult.getResult() != ActionResult.PASS) {
			afterUseHandlers.forEach(sub -> sub.handle(user, user.getStackInHand(hand), hand));
		}
		return finalResult;
	}

	@Override
	public void handle(Entity source, ItemStack target, Hand hand) {
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return action;
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return maxUseTime;
	}

	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		baseHandler.usageTick(world, user, stack, remainingUseTicks);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		return baseHandler.finishUsing(stack, world, user);
	}

	@Override
	public void stoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		onStoppedUsing.forEach(sub -> sub.stoppedUsing(stack, world, user, remainingUseTicks));
	}

	@Override
	public boolean isUsedOnRelease(ItemStack stack) {
		return usedOnRelease;
	}
}
