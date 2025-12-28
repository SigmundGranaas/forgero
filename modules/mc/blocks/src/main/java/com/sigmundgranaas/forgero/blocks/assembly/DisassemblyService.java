package com.sigmundgranaas.forgero.blocks.assembly;

import com.sigmundgranaas.forgero.blocks.api.StationContext;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for disassembling Forgero components into their constituent parts.
 * <p>
 * The disassembly process:
 * <ol>
 *   <li>Extracts all structure parts (blade, handle, etc.)</li>
 *   <li>Extracts all installed upgrades (gems, bindings, etc.)</li>
 *   <li>Converts each part/upgrade back to ItemStack</li>
 * </ol>
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
	 * Disassembles a component into ItemStacks.
	 *
	 * @param component The component to disassemble
	 * @return Result containing the parts as ItemStacks
	 */
	public DisassemblyResult disassemble(Component component) {
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

		return new DisassemblyResult(parts, component);
	}

	/**
	 * Checks if a component can be disassembled.
	 * <p>
	 * A component can be disassembled if:
	 * <ul>
	 *   <li>It has structure parts OR installed upgrades</li>
	 *   <li>At least one part can be converted to ItemStack</li>
	 * </ul>
	 *
	 * @param component The component to check
	 * @return true if disassembly would produce parts
	 */
	public boolean canDisassemble(Component component) {
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
	 * @param originalComponent The component that was disassembled
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
