package com.sigmundgranaas.forgero.minecraft.common.feature.onhit.entity;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;

import static com.sigmundgranaas.forgero.core.util.match.Matchable.DEFAULT_TRUE;
import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.cachedFilteredFeatures;
import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.cachedRootFeatures;
import static com.sigmundgranaas.forgero.minecraft.common.feature.swinghand.SwingHandFeatureExecutor.isCoolingDownStack;

/**
 * Executor for applying entity tick features at every tick.
 * Will not execute
 * Will make sure conditions are applied before executing the handlers.
 */
public record OnHitEntityFeatureExecutor(List<OnHitEntityFeature> features,
											 ItemStack stack,
											 Hand hand,
											 Entity entity,
											 World world,
											 Entity target) {

		public static OnHitEntityFeatureExecutor initFromMainHandStack(LivingEntity entity, Entity target) {
			ItemStack source = entity.getMainHandStack();
			List<OnHitEntityFeature> features = cachedRootFeatures(source, OnHitEntityFeature.KEY);
			return new OnHitEntityFeatureExecutor(features, source, Hand.MAIN_HAND, entity, entity.getWorld(), target);
		};

		public void executeIfNotCoolingDown(MatchContext matchContext){
			if (isCoolingDownStack(stack, entity)){
				return;
			}

			features.stream()
					.filter(feature -> feature.test(DEFAULT_TRUE, matchContext))
					.forEach(handler -> {
						handler.onHit(entity, world, target);
						handler.handle(entity, stack, hand);
					} );
		}
	}
