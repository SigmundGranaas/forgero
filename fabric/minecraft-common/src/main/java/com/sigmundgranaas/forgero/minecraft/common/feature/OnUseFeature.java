package com.sigmundgranaas.forgero.minecraft.common.feature;

import static com.sigmundgranaas.forgero.minecraft.common.handler.HandlerBuilder.buildHandlerFromJson;

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
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.AfterUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.BaseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.BlockUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.EntityUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.StopHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.UseHandler;

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


public class OnUseFeature extends BasePredicateFeature implements BlockUseHandler, EntityUseHandler, UseHandler, AfterUseHandler, StopHandler {
	public static final String TYPE = "minecraft:on_use";
	public static final ClassKey<OnUseFeature> KEY = new ClassKey<>(TYPE, OnUseFeature.class);
	public static final String USE = "use";
	public static final String ENTIY_USE = "entity";
	public static final String BLOCK_USE = "block";
	public static final String STOP_USE = "on_stop";
	public static final String AFTER_USE = "after";

	public static final FeatureBuilder<OnUseFeature> BUILDER = FeatureBuilder.of(TYPE, OnUseFeature::buildFromBase);

	private final List<UseHandler> onUse;

	private final List<EntityUseHandler> useOnEntity;

	private final List<BlockUseHandler> useOnBlock;

	private final List<StopHandler> onStoppedUsing;

	private final List<AfterUseHandler> afterUseHandlers;

	private final BaseHandler baseHandler;

	private final UseAction action;

	private final int maxUseTime;

	public OnUseFeature(BasePredicateData data, List<UseHandler> onUse, List<EntityUseHandler> useOnEntity, List<BlockUseHandler> useOnBlock, List<StopHandler> onStoppedUsing, List<AfterUseHandler> afterUseHandlers, BaseHandler baseHandler, UseAction action, int maxUseTime) {
		super(data);
		this.onUse = onUse;
		this.useOnEntity = useOnEntity;
		this.useOnBlock = useOnBlock;
		this.onStoppedUsing = onStoppedUsing;
		this.afterUseHandlers = afterUseHandlers;
		this.baseHandler = baseHandler;
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

		return new OnUseFeature(data, use, entity, block, stop, afterUseHandler, base, action, maxUseTime);
	}

	private static <T> List<T> parseHandler(ClassKey<T> key, JsonElement element, String jsonKey) {
		if (element.isJsonObject() && element.getAsJsonObject().has(jsonKey)) {
			var object = element.getAsJsonObject();
			var handlerOpt = HandlerBuilder.DEFAULT.build(key, object.get(jsonKey));
			return handlerOpt.map(List::of).orElse(Collections.emptyList());
		} else if (element.isJsonArray()) {
			var elements = element.getAsJsonArray();
			var handlers = new ArrayList<T>();
			for (JsonElement jsonElement : elements) {
				if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has(jsonKey)) {
					var object = jsonElement.getAsJsonObject();
					HandlerBuilder.DEFAULT.build(key, object.get(jsonKey)).ifPresent(handlers::add);
				}
			}
			if (!handlers.isEmpty()) {
				return handlers;
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
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if (onUse.isEmpty()) {
			return TypedActionResult.pass(user.getStackInHand(hand));
		}
		TypedActionResult<ItemStack> finalResult = TypedActionResult.pass(user.getStackInHand(hand));
		for (UseHandler handler : onUse) {
			var result = handler.use(world, user, hand);
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
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		onStoppedUsing.forEach(sub -> sub.onStoppedUsing(stack, world, user, remainingUseTicks));
	}

	@Override
	public boolean isUsedOnRelease(ItemStack stack) {
		return baseHandler.isUsedOnRelease(stack);
	}
}
