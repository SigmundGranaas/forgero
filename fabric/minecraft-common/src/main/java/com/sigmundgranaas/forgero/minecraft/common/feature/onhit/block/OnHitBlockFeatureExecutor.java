package com.sigmundgranaas.forgero.minecraft.common.feature.onhit.block;

import static com.sigmundgranaas.forgero.core.util.match.Matchable.DEFAULT_TRUE;
import static com.sigmundgranaas.forgero.minecraft.common.feature.FeatureUtils.cachedFilteredFeatures;
import static com.sigmundgranaas.forgero.minecraft.common.feature.swinghand.SwingHandFeatureExecutor.isCoolingDownStack;

import java.util.List;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Executor for applying block hit feature every time an entity is hitting a block.
 * Will make sure conditions are applied before executing the handlers and afteruse handlers.
 */
public record OnHitBlockFeatureExecutor(List<OnHitBlockFeature> features,
                                        ItemStack stack,
                                        Hand hand,
                                        Entity entity,
                                        World world,
                                        BlockPos target) {

		public static OnHitBlockFeatureExecutor initFromMainHandStack(LivingEntity entity, BlockPos target, MatchContext matchContext) {
			ItemStack source = entity.getMainHandStack();
			List<OnHitBlockFeature> features = cachedFilteredFeatures(source, OnHitBlockFeature.KEY, matchContext);
			return new OnHitBlockFeatureExecutor(features, source, Hand.MAIN_HAND, entity, entity.getWorld(), target);
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
