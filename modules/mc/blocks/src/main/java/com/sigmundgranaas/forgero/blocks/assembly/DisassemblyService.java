package com.sigmundgranaas.forgero.blocks.assembly;

import com.sigmundgranaas.forgero.blocks.api.StationContext;
import com.sigmundgranaas.forgero.blocks.assembly.DisassemblyRecipeLoader.DisassemblyRecipe;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service for disassembling items into their constituent parts.
 * <p>
 * Supports two types of disassembly:
 * <ol>
 *   <li><b>Component-based</b>: Forgero components are disassembled into structure parts and upgrades</li>
 *   <li><b>Recipe-based</b>: Non-Forgero items are disassembled using JSON recipes</li>
 * </ol>
 * <p>
 * The service tries component-based disassembly first, then falls back to recipe-based.
 */
public class DisassemblyService {

	private final StationContext context;

	/**
	 * Creates a new disassembly service.
	 *
	 * @param context The station context with services
	 */
	public DisassemblyService(StationContext context) {
		this.context = context;
	}

	/**
	 * Disassembles an ItemStack into parts.
	 * <p>
	 * First attempts component-based disassembly, then falls back to recipe-based.
	 *
	 * @param stack The ItemStack to disassemble
	 * @return Result containing the parts as ItemStacks
	 */
	public DisassemblyResult disassemble(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return DisassemblyResult.empty();
		}

		// Try component-based disassembly first
		Optional<Component> component = context.converter().toComponent(stack);
		if (component.isPresent()) {
			DisassemblyResult result = disassembleComponent(component.get());
			if (!result.isEmpty()) {
				return result;
			}
		}

		// Fall back to recipe-based disassembly
		return disassembleByRecipe(stack);
	}

	/**
	 * Disassembles a component into ItemStacks.
	 *
	 * @param component The component to disassemble
	 * @return Result containing the parts as ItemStacks
	 */
	public DisassemblyResult disassembleComponent(Component component) {
		if (component == null) {
			return DisassemblyResult.empty();
		}

		List<ItemStack> parts = new ArrayList<>();

		// Get structure parts
		List<ComponentPart> structureParts = context.slotManager().getStructureParts(component);
		for (ComponentPart part : structureParts) {
			context.converter().toStack(part.content()).ifPresent(parts::add);
		}

		// Get installed upgrades
		List<Component> upgrades = context.slotManager().getInstalledUpgrades(component);
		for (Component upgrade : upgrades) {
			context.converter().toStack(upgrade).ifPresent(parts::add);
		}

		return new DisassemblyResult(parts, null);
	}

	/**
	 * Disassembles an ItemStack using recipe definitions.
	 *
	 * @param stack The ItemStack to disassemble
	 * @return Result containing the parts as ItemStacks
	 */
	private DisassemblyResult disassembleByRecipe(ItemStack stack) {
		for (DisassemblyRecipe recipe : DisassemblyRecipeLoader.getRecipes()) {
			if (recipe.getInput().test(stack)) {
				List<ItemStack> parts = new ArrayList<>();
				for (Item item : recipe.getResults()) {
					parts.add(new ItemStack(item));
				}
				return new DisassemblyResult(parts, null);
			}
		}
		return DisassemblyResult.empty();
	}

	/**
	 * Checks if an ItemStack can be disassembled.
	 * <p>
	 * An item can be disassembled if:
	 * <ul>
	 *   <li>It's a Forgero component with structure parts or upgrades, OR</li>
	 *   <li>It matches a disassembly recipe</li>
	 * </ul>
	 *
	 * @param stack The ItemStack to check
	 * @return true if disassembly would produce parts
	 */
	public boolean canDisassemble(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}

		// Check if it's a damageable item that's damaged
		if (stack.isDamageable() && stack.getDamage() > 0) {
			return false;
		}

		// Try component-based
		Optional<Component> component = context.converter().toComponent(stack);
		if (component.isPresent() && canDisassembleComponent(component.get())) {
			return true;
		}

		// Try recipe-based
		return hasMatchingRecipe(stack);
	}

	/**
	 * Checks if a component can be disassembled.
	 */
	private boolean canDisassembleComponent(Component component) {
		if (component == null) {
			return false;
		}

		// Check if there are any structure parts
		List<ComponentPart> parts = context.slotManager().getStructureParts(component);
		if (!parts.isEmpty()) {
			return true;
		}

		// Check if there are any upgrades
		List<Component> upgrades = context.slotManager().getInstalledUpgrades(component);
		return !upgrades.isEmpty();
	}

	/**
	 * Checks if there's a recipe that matches this ItemStack.
	 */
	private boolean hasMatchingRecipe(ItemStack stack) {
		for (DisassemblyRecipe recipe : DisassemblyRecipeLoader.getRecipes()) {
			if (recipe.getInput().test(stack)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the given ItemStacks match the expected disassembly result.
	 * <p>
	 * Used to verify that items removed from result slots match expected parts.
	 *
	 * @param result       The expected disassembly result
	 * @param removedItems The items that were removed
	 * @return true if all expected parts were removed
	 */
	public boolean isDisassembled(DisassemblyResult result, List<ItemStack> removedItems) {
		if (result.isEmpty()) {
			return removedItems.isEmpty();
		}

		// Check if removed items count matches expected
		int expectedCount = result.parts().size();
		int actualCount = (int) removedItems.stream().filter(s -> !s.isEmpty()).count();

		return actualCount == expectedCount;
	}

	/**
	 * Factory method.
	 */
	public static DisassemblyService create(StationContext context) {
		return new DisassemblyService(context);
	}

	/**
	 * Result of a disassembly operation.
	 *
	 * @param parts             List of ItemStacks representing the parts
	 * @param originalComponent The component that was disassembled (null for recipe-based)
	 */
	public record DisassemblyResult(
			List<ItemStack> parts,
			Component originalComponent
	) {
		/**
		 * Empty result (nothing to disassemble).
		 */
		public static DisassemblyResult empty() {
			return new DisassemblyResult(Collections.emptyList(), null);
		}

		/**
		 * Checks if this result is empty.
		 */
		public boolean isEmpty() {
			return parts.isEmpty();
		}

		/**
		 * Gets the number of parts.
		 */
		public int size() {
			return parts.size();
		}
	}
}
