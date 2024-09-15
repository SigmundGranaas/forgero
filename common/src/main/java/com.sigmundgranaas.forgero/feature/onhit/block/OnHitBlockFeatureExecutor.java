package com.sigmundgranaas.forgero.feature.onhit.block;

import static com.sigmundgranaas.forgero.core.util.match.Matchable.DEFAULT_TRUE;
import static com.sigmundgranaas.forgero.feature.FeatureUtils.cachedFilteredFeatures;
import static com.sigmundgranaas.forgero.feature.FeatureUtils.cachedRootFeatures;
import static com.sigmundgranaas.forgero.feature.swinghand.SwingHandFeatureExecutor.isCoolingDownStack;

import java.util.List;

import com.sigmundgranaas.forgero.core.util.match.MatchContext;

import com.sigmundgranaas.forgero.feature.tick.EntityTickFeature;
import com.sigmundgranaas.forgero.feature.tick.EntityTickFeatureExecutor;
import com.sigmundgranaas.forgero.item.DefaultStateItem;
import com.sigmundgranaas.forgero.item.ToolStateItem;

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

		public static OnHitBlockFeatureExecutor initFromMainHandStack(LivingEntity entity, BlockPos target) {
			ItemStack source = entity.getMainHandStack();
			List<OnHitBlockFeature> features = cachedRootFeatures(source, OnHitBlockFeature.KEY);
			return new OnHitBlockFeatureExecutor(features, source, Hand.MAIN_HAND, entity, entity.getWorld(), target);
		};
	public static OnHitBlockFeatureExecutor initFromStack(ItemStack stack, Entity entity, BlockPos target) {
		List<OnHitBlockFeature> features = cachedRootFeatures(stack, OnHitBlockFeature.KEY);
		return new OnHitBlockFeatureExecutor(features, stack, Hand.MAIN_HAND, entity,  entity.getWorld(), target);
	};

		public void executeIfNotCoolingDown(MatchContext matchContext){
			if(stack != null && stack.getItem() instanceof DefaultStateItem){
				return;
			}

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
