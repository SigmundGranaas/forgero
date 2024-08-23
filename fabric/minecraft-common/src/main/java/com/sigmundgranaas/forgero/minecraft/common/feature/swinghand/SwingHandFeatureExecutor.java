package com.sigmundgranaas.forgero.minecraft.common.feature.swinghand;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.List;

import static com.sigmundgranaas.forgero.core.util.match.Matchable.DEFAULT_TRUE;
import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.cachedFilteredFeatures;

/**
 * Executor for ensuring that the features are filtered and applied correctly.
 * Dynamic features should be filtered
 * Handler should be executed once
 * AfterUse handlers should be executed after that
 * <p>
 * Currently, all features are gathered from the main hand stack of the living entity.
 *
 * @param features
 * @param entity
 * @param stack
 * @param hand
 */
public record SwingHandFeatureExecutor(List<SwingHandFeature> features,
		 LivingEntity entity,
		 ItemStack stack,
		 Hand hand) {

	public static SwingHandFeatureExecutor initFromMainHandStack(LivingEntity entity, ItemStack stack, Hand hand, MatchContext matchContext) {
		List<SwingHandFeature> features = cachedFilteredFeatures(stack, SwingHandFeature.KEY, matchContext);
		return new SwingHandFeatureExecutor(features, entity, stack, hand);
	};

	public static boolean isCoolingDownStack(ItemStack stack, Entity entity) {
		if (entity instanceof PlayerEntity player) {
			return player.getItemCooldownManager().isCoolingDown(stack.getItem());
		}
		return false;
	}

	public void execute(MatchContext matchContext){
		if (isCoolingDownStack(stack, entity)){
			return;
		}

		features.stream()
				.filter(feature -> feature.test(DEFAULT_TRUE, matchContext))
				.forEach(handler -> {
					handler.onSwing(entity, hand);
					handler.handle(entity,  stack, hand);
				});
	}
}