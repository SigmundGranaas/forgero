package com.sigmundgranaas.forgero.tools;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.component.mutation.impl.ComponentMutaterImpl;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Handles the game logic for slotting a Forgero component (from the off-hand)
 * into an empty slot of a customizable Forgero item (in the main-hand).
 */
public class ComponentSlottingHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(ComponentSlottingHandler.class);
	private final ComponentConverter converter;

	public ComponentSlottingHandler(ComponentConverter converter) {
		this.converter = converter;
	}

	/**
	 * Executes the slotting logic when a player uses an item.
	 *
	 * @param player The player using the item.
	 * @param world  The world the player is in.
	 * @param hand   The hand being used.
	 * @return A TypedActionResult indicating the result of the action.
	 */
	public TypedActionResult<ItemStack> handle(PlayerEntity player, World world, Hand hand) {
		if (world.isClient() || hand != Hand.MAIN_HAND) {
			return TypedActionResult.pass(player.getStackInHand(hand));
		}

		ItemStack mainHandStack = player.getStackInHand(Hand.MAIN_HAND);
		ItemStack offHandStack = player.getStackInHand(Hand.OFF_HAND);

		Optional<Component> mainComponentOpt = converter.toComponent(mainHandStack);
		Optional<Component> offHandComponentOpt = converter.toComponent(offHandStack);

		// Ensure both items are valid Forgero components
		if (mainComponentOpt.isEmpty() || offHandComponentOpt.isEmpty()) {
			return TypedActionResult.pass(mainHandStack);
		}

		Component mainComponent = mainComponentOpt.get();
		Component offHandComponent = offHandComponentOpt.get();

		// Ensure the main-hand component is customizable
		if (!(mainComponent instanceof CustomizableComponent customizable)) {
			return TypedActionResult.pass(mainHandStack);
		}

		for (UpgradeSlot slot : customizable.getUpgradeSlots()) {
			// Find the first empty slot that accepts the off-hand component
			if (slot.content().isEmpty() && slot.validator().test(offHandComponent)) {
				return performSlotUpgrade(player, mainComponent, offHandComponent, slot, mainHandStack, offHandStack);
			}
		}

		return TypedActionResult.pass(mainHandStack);
	}

	private TypedActionResult<ItemStack> performSlotUpgrade(PlayerEntity player, Component mainComponent, Component upgrade, UpgradeSlot slot, ItemStack originalStack, ItemStack upgradeStack) {
		ComponentMutater mutater = new ComponentMutaterImpl();
		Component newMainComponent = mutater.setSlot(mainComponent, slot.id(), upgrade);

		// Use the converter to create a new stack with the updated component state
		Optional<ItemStack> newStackOpt = converter.toStack(newMainComponent);

		if (newStackOpt.isEmpty()) {
			LOGGER.warn("Failed to create upgraded item. No item mapping registered for new component ID: {}", newMainComponent.id());
			player.sendMessage(Text.translatable("forgero.upgrade.error"), true);
			return TypedActionResult.fail(originalStack);
		}

		ItemStack newStack = newStackOpt.get();
		player.setStackInHand(Hand.MAIN_HAND, newStack);

		if (!player.isCreative()) {
			upgradeStack.decrement(1);
		}

		player.sendMessage(Text.translatable("forgero.upgrade.success", upgrade.id().path(), mainComponent.id().path()), true);
		return TypedActionResult.success(newStack, true);
	}
}
