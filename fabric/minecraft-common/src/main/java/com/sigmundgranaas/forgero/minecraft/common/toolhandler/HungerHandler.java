package com.sigmundgranaas.forgero.minecraft.common.toolhandler;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

/**
 * Applies scaled exhaustion to the player based on the tool's weight,
 * in line with Minecraft's native hunger mechanics.
 */
public class HungerHandler {
	private final StateService service;

	public HungerHandler(StateService service) {
		this.service = service;
	}

	public ActionResult handle(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		ItemStack stack = player.getStackInHand(hand);
		handle(player, stack);
		return ActionResult.PASS;
	}

	public void handle(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
		ItemStack stack = player.getMainHandStack();
		handle(player, stack);
	}

	private void handle(PlayerEntity player, ItemStack stack) {
		if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
		if (!ForgeroConfigurationLoader.configuration.weightIncreasesHunger) return;

		service.convert(stack)
				.filter(state -> state.test(Type.TOOL) || state.test(Type.WEAPON))
				.map(state -> ComputedAttribute.of(state, Weight.KEY))
				.map(ComputedAttribute::asInt)
				.ifPresent(weight -> applyWeightExhaustion(serverPlayer, weight));
	}

	/**
	 * Applies scaled exhaustion based on tool weight.
	 * Vanilla mining adds 0.005 exhaustion.
	 * Weight of 0–100 scales linearly (or with a tweakable curve).
	 */
	private void applyWeightExhaustion(ServerPlayerEntity player, int weight) {
		float exhaustion = 0.0002f * weight - 0.005f;
		exhaustion = Math.max(0f, exhaustion); // Clamp to prevent negative exhaustion
		player.getHungerManager().addExhaustion(exhaustion);
	}
}
