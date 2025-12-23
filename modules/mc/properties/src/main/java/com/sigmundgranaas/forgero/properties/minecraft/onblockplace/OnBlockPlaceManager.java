package com.sigmundgranaas.forgero.properties.minecraft.onblockplace;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.effects.block.BlockEffect;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/**
 * Manager for handling On-Block-Place events.
 * Orchestrates property resolution and effect application when players place blocks.
 */
public class OnBlockPlaceManager {
	private OnBlockPlaceManager() {
		// Static class
	}

	/**
	 * Called when a player places a block.
	 * Resolves OnBlockPlaceProperty from the held item and applies block effects.
	 *
	 * @param player The player who placed the block
	 * @param pos The position where the block was placed
	 * @param stack The item stack used to place the block
	 */
	public static void handleBlockPlace(PlayerEntity player, BlockPos pos, ItemStack stack) {
		if (player.getWorld().isClient()) {
			return; // Server-side only
		}

		if (stack.isEmpty()) {
			return;
		}

		// Convert item to component and resolve properties
		ForgeroApi.converter().toComponent(stack).ifPresent(component -> {
			List<OnBlockPlaceProperty> properties = getActiveProperties(component);

			for (OnBlockPlaceProperty property : properties) {
				BlockState placedState = player.getWorld().getBlockState(pos);

				// Use selector to determine which positions to affect
				List<BlockPos> targetPositions = property.selector().select(player, pos, placedState);

				// Apply each effect to each selected position
				for (BlockPos targetPos : targetPositions) {
					BlockState targetState = player.getWorld().getBlockState(targetPos);

					for (BlockEffect effect : property.effects()) {
						effect.apply(player, targetPos, targetState);
					}
				}
			}
		});
	}

	/**
	 * Resolves active OnBlockPlaceProperty instances from a component.
	 * Applies conditions and context to determine which properties are active.
	 *
	 * @param component The component to resolve properties from
	 * @return List of active properties
	 */
	private static List<OnBlockPlaceProperty> getActiveProperties(Component component) {
		var engine = new OnBlockPlaceProperty.Engine();
		DynamicContext.Builder contextBuilder = new DynamicContext.Builder();

		// Additional context could be added here (biome, time of day, etc.)
		// For now, use empty context

		return ForgeroApi.resolver().resolve(component, engine, contextBuilder.build());
	}
}
