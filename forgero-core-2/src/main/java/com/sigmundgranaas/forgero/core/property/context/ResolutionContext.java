package com.sigmundgranaas.forgero.core.property.context;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.condition.StaticCondition;

import java.util.*;

/**
 * Provides rich, contextual information during the static "Bake" phase of property resolution.
 * Upon creation, it traverses the entire component tree once to build a complete topological map.
 * This map allows any component to query its position, containment, and relationships within the
 * larger assembly, enabling highly advanced conditional properties. This object is immutable
 * after construction.
 */
public class ResolutionContext {
	private final Component self;
	private final Component root;
	private final Map<Component, Slot> slotMap;
	private final Map<Component, Component> parentMap;
	private final Map<Component, Integer> depthMap;

	/**
	 * Constructs a new ResolutionContext. The constructor is responsible for performing
	 * the single traversal needed to build the internal context maps.
	 *
	 * @param self The component currently being evaluated, for which this context is created.
	 * @param root The root component of the entire assembly being resolved.
	 */
	public ResolutionContext(Component self, Component root) {
		this.self = self;
		this.root = root;
		this.slotMap = new HashMap<>();
		this.parentMap = new HashMap<>();
		this.depthMap = new HashMap<>();
		buildContextMaps(root, null, 0);
	}

	private void buildContextMaps(Component current, Component parent, int depth) {
		parentMap.put(current, parent);
		depthMap.put(current, depth);

		if (current instanceof StructuredComponent structured) {
			for (StructureSlot slot : structured.structure().slots()) {
				slotMap.put(slot.content(), slot);
				buildContextMaps(slot.content(), current, depth + 1);
			}
		}
		if (current instanceof CustomizableComponent customizable) {
			for (UpgradeSlot slot : customizable.upgrades().slots()) {
				slot.content().ifPresent(child -> {
					slotMap.put(child, slot);
					buildContextMaps(child, current, depth + 1);
				});
			}
		}
	}

	/** @return The component that owns the property being checked. */
	public Component self() { return self; }

	/** @return The top-group component in the resolution tree. */
	public Component root() { return root; }

	/** @return An Optional containing the Slot the 'self' component is contained within. Empty if 'self' is the root. */
	public Optional<Slot> getSlot() { return Optional.ofNullable(slotMap.get(self)); }

	/** @return A list of the 'self' component's siblings (other components sharing the same parent). */
	public List<Component> getSiblings() {
		return getParent()
				.map(Component::getChildren)
				.orElse(Collections.emptyList())
				.stream()
				.filter(comp -> comp != self)
				.toList();
	}

	/**
	 * Recursively finds the first component that fills a slot of the given type, starting from the root.
	 * This allows a component to query the contents of other, unrelated slots in the same assembly.
	 *
	 * @param slotType The type identifier of the slot to find.
	 * @return An Optional containing the component in the slot, or empty if not found.
	 */
	public Optional<Component> findInRoot(OpenIdentifier slotType) {
		return findIn(root, slotType);
	}

	private Optional<Component> findIn(Component component, OpenIdentifier slotType) {
		if (component instanceof StructuredComponent structured) {
			for (StructureSlot slot : structured.structure().slots()) {
				if (slot.type().equals(slotType)) {
					return Optional.of(slot.content());
				}
				Optional<Component> nestedResult = findIn(slot.content(), slotType);
				if (nestedResult.isPresent()) {
					return nestedResult;
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * A convenience method to test if a list of static conditions are all met.
	 *
	 * @param conditions The list of conditions to test.
	 * @return true if all conditions pass, false otherwise.
	 */
	public boolean test(List<StaticCondition> conditions) {
		return conditions.stream().allMatch(cond -> cond.test(this));
	}


	/** @return The depth of the 'self' component in the tree, where the root is at depth 0. */
	public int getDepth() { return depthMap.getOrDefault(self, -1); }

	/** @return true if the 'self' component is the root of the resolution tree. */
	public boolean isRoot() { return self == root; }

	/** @return An Optional containing the direct parent of the 'self' component. Empty if 'self' is the root. */
	public Optional<Component> getParent() { return Optional.ofNullable(parentMap.get(self)); }
}
