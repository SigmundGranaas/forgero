package com.sigmundgranaas.forgero.minecraft.common.toolhandler;


import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Weight;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import org.jetbrains.annotations.Nullable;

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

public class HungerHandler {
	private final StateService service;

	public HungerHandler(StateService service) {
		this.service = service;
	}

	public ActionResult handle(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult) {
		ItemStack stack = player.getStackInHand(hand);
		handleCombat(player, stack);
		return ActionResult.PASS;
	}

	public void handle(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
		ItemStack stack = player.getMainHandStack();
		handleMining(player, stack);
	}

	private void handleCombat(PlayerEntity player, ItemStack stack) {
		if (player instanceof ServerPlayerEntity serverPlayerEntity && ForgeroConfigurationLoader.configuration.weightIncreasesHunger) {
			service.convert(stack)
					.filter(state -> state.test(Type.TOOL) || state.test(Type.WEAPON))
					.map(state -> ComputedAttribute.of(state, Weight.KEY))
					.map(ComputedAttribute::asInt)
					.ifPresent(weight -> addCombatExhaustionBasedOnWeight(serverPlayerEntity, weight));
		}
	}

	private void handleMining(PlayerEntity player, ItemStack stack) {
		if (player instanceof ServerPlayerEntity serverPlayerEntity && ForgeroConfigurationLoader.configuration.weightIncreasesHunger) {
			service.convert(stack)
					.filter(state -> state.test(Type.TOOL) || state.test(Type.WEAPON)) //
					.map(state -> ComputedAttribute.of(state, Weight.KEY))
					.map(ComputedAttribute::asInt)
					.ifPresent(weight -> addMiningExhaustionBasedOnWeight(serverPlayerEntity, weight));
		}
	}

	private void addCombatExhaustionBasedOnWeight(ServerPlayerEntity player, int weight) {
		weight = Math.max(0, Math.min(weight, 30));
		float exhaustion = 0.02f * weight;
		player.getHungerManager().addExhaustion(exhaustion);
	}

	private void addMiningExhaustionBasedOnWeight(ServerPlayerEntity player, int weight) {
		weight = Math.max(0, Math.min(weight, 30));
		float exhaustion = 0.001f * weight;
		player.getHungerManager().addExhaustion(exhaustion);
	}
}
