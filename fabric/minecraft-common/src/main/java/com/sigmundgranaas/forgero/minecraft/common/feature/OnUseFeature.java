package com.sigmundgranaas.forgero.minecraft.common.feature;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.v2.feature.*;
import com.sigmundgranaas.forgero.minecraft.common.handler.afterUse.AfterUseHandler;
import com.sigmundgranaas.forgero.minecraft.common.handler.use.*;
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

import javax.annotation.Nullable;
import java.util.Optional;


public class OnUseFeature extends BasePredicateFeature implements BlockUseHandler, EntityUseHandler, UseHandler, AfterUseHandler, StopHandler {
	public static final String TYPE = "minecraft:on_use";
	public static final ClassKey<OnUseFeature> KEY = new ClassKey<>(TYPE, OnUseFeature.class);
	public static final String USE = "use";
	public static final String ENTIY_USE = "entity";
	public static final String BLOCK_USE = "block";
	public static final String STOP_USE = "on_stop";

	public static final String AFTER_USE = "after";

	public static final FeatureBuilder<OnUseFeature> BUILDER = FeatureBuilder.of(TYPE, OnUseFeature::buildFromBase);

	@Nullable
	private final UseHandler onUse;

	@Nullable
	private final EntityUseHandler useOnEntity;

	@Nullable
	private final BlockUseHandler useOnBlock;

	@Nullable
	private final StopHandler onStoppedUsing;

	private final BaseHandler baseHandler;

	private final UseAction action;

	private final int maxUseTime;

	public OnUseFeature(BasePredicateData data, @Nullable UseHandler onUse, @Nullable EntityUseHandler useOnEntity, @Nullable BlockUseHandler useOnBlock, @Nullable StopHandler onStoppedUsing, BaseHandler baseHandler, UseAction action, int maxUseTime) {
		super(data);
		this.onUse = onUse;
		this.useOnEntity = useOnEntity;
		this.useOnBlock = useOnBlock;
		this.onStoppedUsing = onStoppedUsing;
		this.baseHandler = baseHandler;
		this.action = action;
		this.maxUseTime = maxUseTime;

		if (!data.type().equals(TYPE)) {
			throw new IllegalArgumentException("Type needs to be: " + TYPE);
		}
	}

	private static OnUseFeature buildFromBase(BasePredicateData data, JsonElement element) {
		UseHandler use = parseHandler(UseHandler.KEY, element, USE);

		EntityUseHandler entity = parseHandler(EntityUseHandler.KEY, element, ENTIY_USE);

		BlockUseHandler block = parseHandler(BlockUseHandler.KEY, element, BLOCK_USE);

		StopHandler stop = parseHandler(StopHandler.KEY, element, STOP_USE);

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

		var base = Optional.ofNullable((BaseHandler) use)
				.or(() -> Optional.ofNullable(entity))
				.or(() -> Optional.ofNullable(block))
				.orElse(BaseHandler.DEFAULT);

		return new OnUseFeature(data, use, entity, block, stop, base, action, maxUseTime);
	}

	@Nullable
	private static <T> T parseHandler(ClassKey<T> key, JsonElement element, String jsonKey) {
		if (element.isJsonObject() && element.getAsJsonObject().has(jsonKey)) {
			var object = element.getAsJsonObject();
			var handlerOpt = HandlerBuilder.DEFAULT.build(key, object.get(jsonKey));
			return handlerOpt.orElse(null);
		} else {
			return null;
		}
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		return Optional.ofNullable(useOnBlock)
				.map(handler -> handler.useOnBlock(context))
				.orElse(ActionResult.PASS);
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		return Optional.ofNullable(useOnEntity)
				.map(handler -> handler.useOnEntity(stack, user, entity, hand))
				.orElse(ActionResult.PASS);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		return Optional.ofNullable(onUse)
				.map(handler -> handler.use(world, user, hand))
				.orElse(TypedActionResult.pass(user.getStackInHand(hand)));
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
		Optional.ofNullable(onStoppedUsing)
				.ifPresent(handler -> handler.onStoppedUsing(stack, world, user, remainingUseTicks));
	}

	@Override
	public boolean isUsedOnRelease(ItemStack stack) {
		return baseHandler.isUsedOnRelease(stack);
	}
}
