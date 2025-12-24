package com.sigmundgranaas.forgero.properties.minecraft.blockuse;

import com.sigmundgranaas.forgero.common.useinteraction.BlockUseContext;
import com.sigmundgranaas.forgero.common.useinteraction.BlockUseEffect;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;

import java.util.List;

/**
 * Manager class for handling block use events.
 * Resolves BlockUseProperty from items and executes their effects.
 */
public class BlockUseManager {

	private BlockUseManager() {
		// Static class
	}

	/**
	 * Handles the event when a player uses an item on a block.
	 *
	 * @param stack The item stack used
	 * @param player The player using the item
	 * @param hand The hand holding the item
	 * @param hitResult The block hit result
	 * @return The result of the interaction
	 */
	public static ActionResult handleBlockUse(ItemStack stack, PlayerEntity player, Hand hand, BlockHitResult hitResult) {
		if (stack.isEmpty() || player.getWorld().isClient()) {
			return ActionResult.PASS;
		}

		return ForgeroApi.converter().toComponent(stack)
				.map(component -> {
					List<BlockUseProperty> properties = getActiveProperties(component);

					if (properties.isEmpty()) {
						return ActionResult.PASS;
					}

					BlockUseContext context = BlockUseContext.create(
							player.getWorld(),
							player,
							hand,
							stack,
							hitResult
					);

					ActionResult finalResult = ActionResult.PASS;
					for (BlockUseProperty property : properties) {
						for (BlockUseEffect effect : property.effects()) {
							ActionResult result = effect.apply(context);
							if (result == ActionResult.FAIL) {
								return ActionResult.FAIL;
							}
							if (result != ActionResult.PASS) {
								finalResult = result;
							}
						}
					}

					return finalResult;
				})
				.orElse(ActionResult.PASS);
	}

	private static List<BlockUseProperty> getActiveProperties(Component component) {
		var engine = new BlockUseProperty.Engine();
		DynamicContext.Builder contextBuilder = new DynamicContext.Builder();

		// Build context for dynamic condition evaluation
		return ForgeroApi.resolver().resolve(component, engine, contextBuilder.build());
	}
}
