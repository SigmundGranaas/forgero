package com.sigmundgranaas.forgero.properties.minecraft.onhitblock;

import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.RuntimeConditions;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.effects.block.OnHitBlockEffect;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;

/**
 * Manager class for handling block hit events.
 * Resolves OnHitBlockProperty from items and executes their effects.
 */
public class OnHitBlockManager {

	private OnHitBlockManager() {
		// Static class
	}

	/**
	 * Handles the event when an entity hits a block with an item.
	 *
	 * @param stack The item stack used to hit the block
	 * @param world The world where the hit occurred
	 * @param source The entity that hit the block
	 * @param targetPos The position of the block that was hit
	 */
	public static void handleOnHitBlock(ItemStack stack, World world, Entity source, BlockPos targetPos) {
		if (stack.isEmpty() || world.isClient()) {
			return;
		}

		DynamicContext.Builder contextBuilder = new DynamicContext.Builder();
		List<OnHitBlockProperty> properties = RuntimeConditions.filter(ForgeroApi.itemProperty().resolve(stack, OnHitBlockProperty.Engine::new), contextBuilder.build());

		for (OnHitBlockProperty property : properties) {
			// Selector determines which blocks are affected
			Set<BlockPos> selectedBlocks = property.selector().select(targetPos, source);

			// Apply effects to all selected blocks
			for (BlockPos pos : selectedBlocks) {
				for (OnHitBlockEffect effect : property.effects()) {
					effect.apply(world, source, pos);
				}
			}
		}
	}
}
