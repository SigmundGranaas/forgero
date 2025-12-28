package com.sigmundgranaas.forgero.blocks.upgrade;

import com.sigmundgranaas.forgero.blocks.api.ComponentTreeBuilder;
import com.sigmundgranaas.forgero.blocks.api.StationContext;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Builds a slot tree from a component for the Upgrade Station UI.
 * <p>
 * This builder traverses a component's structure and upgrade slots,
 * creating a hierarchical tree representation suitable for the layout engine.
 * <p>
 * The tree includes:
 * <ul>
 *   <li>Structure parts (display only, shows composition)</li>
 *   <li>Upgrade slots (editable, can add/remove upgrades)</li>
 * </ul>
 */
public class UpgradeTreeBuilder implements ComponentTreeBuilder {

	private final StationContext context;

	/**
	 * Creates a new upgrade tree builder.
	 *
	 * @param context The station context for services
	 */
	public UpgradeTreeBuilder(StationContext context) {
		this.context = context;
	}

	@Override
	public SlotTree buildTree(Component component) {
		if (component == null) {
			return SlotTree.empty();
		}

		SlotNode root = buildNode(component, SlotNodeType.ROOT, null);
		return new SlotTree(root);
	}

	/**
	 * Recursively builds a node from a component.
	 *
	 * @param component The component to build from
	 * @param nodeType  The type for this node
	 * @param slot      The upgrade slot (if this is an upgrade slot node)
	 * @return The built node with all children
	 */
	private SlotNode buildNode(Component component, SlotNodeType nodeType, ComponentUpgradeSlot slot) {
		List<SlotNode> children = new ArrayList<>();

		// Add structure parts (display only)
		List<ComponentPart> parts = context.slotManager().getStructureParts(component);
		for (ComponentPart part : parts) {
			SlotNode partNode = buildNode(
					part.content(),
					SlotNodeType.STRUCTURE_PART,
					null
			);
			children.add(partNode);
		}

		// Add upgrade slots (editable)
		if (component instanceof CustomizableComponent customizable) {
			for (ComponentUpgradeSlot upgradeSlot : customizable.getUpgradeSlots()) {
				SlotNode slotNode = buildUpgradeSlotNode(upgradeSlot);
				children.add(slotNode);
			}
		}

		return new SlotNode(
				component.id(),
				nodeType,
				Optional.of(component),
				slot,
				children
		);
	}

	/**
	 * Builds a node for an upgrade slot.
	 *
	 * @param slot The upgrade slot
	 * @return The built node
	 */
	private SlotNode buildUpgradeSlotNode(ComponentUpgradeSlot slot) {
		Optional<Component> content = slot.content();
		List<SlotNode> children;

		// If the slot has content, recursively build children for that content
		if (content.isPresent()) {
			Component slotContent = content.get();
			List<SlotNode> grandchildren = new ArrayList<>();

			// Add structure parts of the content
			List<ComponentPart> parts = context.slotManager().getStructureParts(slotContent);
			for (ComponentPart part : parts) {
				grandchildren.add(buildNode(part.content(), SlotNodeType.STRUCTURE_PART, null));
			}

			// Add upgrade slots of the content
			if (slotContent instanceof CustomizableComponent customizable) {
				for (ComponentUpgradeSlot subSlot : customizable.getUpgradeSlots()) {
					grandchildren.add(buildUpgradeSlotNode(subSlot));
				}
			}

			children = grandchildren;
		} else {
			children = Collections.emptyList();
		}

		return new SlotNode(
				slot.id(),
				SlotNodeType.UPGRADE_SLOT,
				content,
				slot,
				children
		);
	}

	/**
	 * Factory method for creating a tree builder.
	 *
	 * @param context The station context
	 * @return A new tree builder instance
	 */
	public static UpgradeTreeBuilder create(StationContext context) {
		return new UpgradeTreeBuilder(context);
	}
}
