package com.sigmundgranaas.forgero.handler.entity;

import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.feature.FeatureUtils;
import com.sigmundgranaas.forgero.handler.afterUse.AfterUseHandler;
import com.sigmundgranaas.forgero.handler.swing.EntityHandHandler;
import com.sigmundgranaas.forgero.handler.targeted.onHitBlock.BlockTargetHandler;
import com.sigmundgranaas.forgero.handler.targeted.onHitEntity.EntityTargetHandler;
import com.sigmundgranaas.forgero.handler.use.StopHandler;
import com.sigmundgranaas.forgero.handler.use.UseHandler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@FunctionalInterface
public interface EntityBasedHandler extends BlockTargetHandler, EntityTargetHandler, AfterUseHandler, EntityHandHandler, UseHandler, StopHandler {
	EntityBasedHandler DEFAULT = (entity) -> {
	};
	ClassKey<EntityBasedHandler> KEY = new ClassKey<>("minecraft:entity_handler", EntityBasedHandler.class);

	default ComputedAttribute compute(Attribute base, Entity source) {
		return FeatureUtils.compute(base, source);
	}

	void handle(Entity entity);

	@Override
	default void onHit(Entity root, World world, BlockPos pos) {
		handle(root);
	}

	@Override
	default void onHit(Entity root, World world, Entity target) {
		handle(root);
	}

	@Override
	default void handle(Entity source, ItemStack target, Hand hand) {
		handle(source);
	}

	@Override
	default void onSwing(Entity source, Hand hand) {
		handle(source);
	}

	@Override
	default void stoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
		handle(user);
	}

	@Override
	default TypedActionResult<ItemStack> onUse(World world, PlayerEntity user, Hand hand) {
		handle(user);
		return UseHandler.DEFAULT_ITEM_USE_ACTIONS.get().use(world, user, hand);
	}
}
