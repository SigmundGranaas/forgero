package com.sigmundgranaas.forgero.minecraft.common.toolhandler;


import java.util.Random;

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

/**
 * The HungerHandler class is responsible for adjusting the player's hunger level based on the weight
 * of the tool or weapon they're using.
 *
 * <p>The mechanism is built around the idea of a normal distribution, where the hunger reduction probability
 * is a function of the tool or weapon's weight. A central weight point and a spread/scaler are used
 * to determine the shape of the distribution, while a base chance determines the hunger reduction probability
 * at the center point.
 *
 * <p>This class integrates with the game's mechanics by listening to relevant events, such as
 * entity or block interactions, and adjusting the player's hunger accordingly.
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

	private void handle(PlayerEntity player, ItemStack stack) {
		if (player instanceof ServerPlayerEntity serverPlayerEntity && ForgeroConfigurationLoader.configuration.weightIncreasesHunger) {
			service.convert(stack)
					.filter(state -> state.test(Type.TOOL) || state.test(Type.WEAPON))
					.map(state -> ComputedAttribute.of(state, Weight.KEY))
					.map(ComputedAttribute::asInt)
					.ifPresent(weight -> adjustHungerBasedOnWeight(serverPlayerEntity, weight));
		}
	}

	public void handle(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
		ItemStack stack = player.getMainHandStack();
		handle(player, stack);
	}

	/**
	 * Adjusts the player's hunger level based on their tool weight using a normal distribution
	 * centered around a specified point. The chance of reducing hunger by 1 is determined
	 * by a formula that takes into account the tool's weight, a center point, a scaler,
	 * and a base chance.
	 *
	 * <p>Formula to determine adjusted probability of reducing hunger:
	 * adjustedProb = baseChance + (0.5 * (1.0 + erf((weight - centerPoint) / (scaler * sqrt(2))) - 0.5)
	 *
	 * <p>Where:
	 * - weight: The current weight of the tool.
	 * - centerPoint: The weight at which the probability of reducing hunger is equal to baseChance.
	 * - scaler: Determines the spread or standard deviation of the normal distribution around the centerPoint.
	 * - baseChance: Base chance of reducing hunger when the weight is equal to centerPoint.
	 *
	 * @param player The player whose hunger is to be adjusted.
	 * @param weight The weight of the tool.
	 */
	private void adjustHungerBasedOnWeight(ServerPlayerEntity player, int weight) {
		ConfigurationValues config = getConfigurationValues();
		float z = calculateZScore(weight, config.centerPoint, config.scaler);
		double adjustedProb = calculateAdjustedProbability(z, config.baseChance);

		adjustedProb = adjustProbabilityForSaturation(adjustedProb, player.getHungerManager().getSaturationLevel());
		applyHungerReductionBasedOnProbability(player, adjustedProb);
	}

	private ConfigurationValues getConfigurationValues() {
		return new ConfigurationValues(
				ForgeroConfigurationLoader.configuration.WeightIncreasesHungerCenterPoint,
				ForgeroConfigurationLoader.configuration.WeightIncreasesHungerScaler,
				ForgeroConfigurationLoader.configuration.WeightIncreasesHungerBaseChance
		);
	}

	private float calculateZScore(int weight, float centerPoint, float scaler) {
		return (weight - centerPoint) / scaler;
	}

	private double calculateAdjustedProbability(float z, float baseChance) {
		double prob = 0.5 * (1.0 + erf(z / Math.sqrt(2.0)));
		return baseChance + (prob - 0.5);
	}

	private double adjustProbabilityForSaturation(double probability, float saturation) {
		double saturationAdjustment = 1 - Math.min(saturation / 20.0, 1.0);  // Assuming max saturation is typically 20
		return probability * saturationAdjustment;
	}

	private void applyHungerReductionBasedOnProbability(ServerPlayerEntity player, double probability) {
		if (new Random().nextDouble() < probability) {
			int foodLevel = player.getHungerManager().getFoodLevel();
			player.getHungerManager().setFoodLevel(Math.max(foodLevel - 1, 0));
		}
	}

	/**
	 * Approximates the error function, which is used in calculating the cumulative distribution
	 * function (CDF) for a normal distribution. This approximation is used in the
	 * adjustHungerBasedOnWeight method to adjust the player's hunger based on weight.
	 *
	 * @param z The value for which the error function is calculated.
	 * @return The approximate value of the error function for the given z.
	 */
	private double erf(double z) {
		double t = 1.0 / (1.0 + 0.47047 * Math.abs(z));
		double poly = t * (0.3480242 - 0.0958798 * t + 0.7478556 * t * t);
		return z >= 0 ? 1 - poly * Math.exp(-z * z) : poly * Math.exp(-z * z) - 1;
	}

	private record ConfigurationValues(float centerPoint, float scaler, float baseChance) {
	}

}
