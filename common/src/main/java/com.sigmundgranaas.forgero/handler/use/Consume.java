package com.sigmundgranaas.forgero.handler.use;

import java.util.Objects;

import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class Consume implements UseHandler, EntityUseHandler, BlockUseHandler {
	public static final String TYPE = "minecraft:consume";
	public static final JsonBuilder<Consume> BUILDER = HandlerBuilder.fromObject(Consume.class, (json) -> new Consume());

	@Override
	public TypedActionResult<ItemStack> onUse(World world, PlayerEntity user, Hand hand) {
		user.setCurrentHand(hand);
		return TypedActionResult.consume(user.getStackInHand(hand));
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		Objects.requireNonNull(context.getPlayer()).setCurrentHand(context.getHand());
		return ActionResult.CONSUME;
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		user.setCurrentHand(hand);
		return ActionResult.CONSUME;
	}
}
