package com.sigmundgranaas.forgero.minecraft.common.feature.tick;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;

import com.sigmundgranaas.forgero.minecraft.common.item.DefaultStateItem;
import com.sigmundgranaas.forgero.minecraft.common.item.ToolStateItem;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.List;

import static com.sigmundgranaas.forgero.core.util.match.Matchable.DEFAULT_TRUE;
import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.cachedFilteredFeatures;
import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.cachedRootFeatures;

/**
 * Executor for applying entity tick features at every tick.
 * Will make sure conditions are applied before executing the handlers.
 */
public record EntityTickFeatureExecutor(List<EntityTickFeature> features,
										   ItemStack stack,
										   Hand hand,
										   Entity entity) {

	public static EntityTickFeatureExecutor initFromMainHandStack(LivingEntity entity) {
		ItemStack source = entity.getMainHandStack();
		List<EntityTickFeature> features = cachedRootFeatures(source, EntityTickFeature.KEY);
		return new EntityTickFeatureExecutor(features, source, Hand.MAIN_HAND, entity);
	};

	public static EntityTickFeatureExecutor initFromStack(ItemStack stack, Entity entity) {
		List<EntityTickFeature> features = cachedRootFeatures(stack, EntityTickFeature.KEY);
		return new EntityTickFeatureExecutor(features, stack, Hand.MAIN_HAND, entity);
	};

		public void execute(MatchContext matchContext){
			if(stack != null && stack.getItem() instanceof DefaultStateItem){
				return;
			}

			features.stream()
					.filter(feature -> feature.test(DEFAULT_TRUE, matchContext))
					.forEach(handler -> handler.handle(entity));
		}
}
