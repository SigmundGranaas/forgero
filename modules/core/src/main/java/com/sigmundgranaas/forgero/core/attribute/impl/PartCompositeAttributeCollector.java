package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeScope;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ComponentTreeVisitor;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;

import java.util.*;

import static com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY;

/**
 * Collects attributes with PART_COMPOSITE scope from structure parts.
 *
 * <p>This collector gathers attributes that participate in part composition
 * (shape + material intersection logic). It tracks which source (slot) each
 * attribute came from, which is needed for the intersection composition algorithm.</p>
 *
 * <h2>Collection Rules</h2>
 * <ul>
 *   <li>Only collects attributes with {@link AttributeScope#PART_COMPOSITE} scope</li>
 *   <li>Applies static condition filtering</li>
 *   <li>Does NOT traverse into upgrade slots (they have separate handling)</li>
 *   <li>Tracks source slot for each attribute collection</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * PartCompositeAttributeCollector collector = new PartCompositeAttributeCollector();
 * ComponentTreeWalker.walk(component, collector);
 * Map<String, List<Attribute>> sources = collector.getResult();
 * // sources: {"self" -> [...], "blade" -> [...], "handle" -> [...]}
 * }</pre>
 *
 * @see com.sigmundgranaas.forgero.core.attribute.composition.PartCompositeScopeHandler
 */
public class PartCompositeAttributeCollector implements ComponentTreeVisitor<Map<String, List<Attribute>>> {

	private final Map<String, List<Attribute>> sources = new HashMap<>();
	private final Deque<String> slotStack = new ArrayDeque<>();
	private boolean inUpgradeSlot = false;

	/**
	 * Creates a new collector with "self" as the initial source.
	 */
	public PartCompositeAttributeCollector() {
		slotStack.push("self");
	}

	@Override
	public boolean visit(Component component, Component root, int depth) {
		// Don't collect from upgrade slots - they have their own handling
		if (inUpgradeSlot) {
			return false;
		}

		ResolutionContext ctx = new ResolutionContext(component, root);
		String currentSource = slotStack.peek();

		List<Attribute> partCompositeAttrs = component.properties(KEY).stream()
				.filter(attr -> attr.scope()
						.map(s -> s.equals(AttributeScope.PART_COMPOSITE))
						.orElse(false))
				.filter(attr -> attr.condition()
						.map(Condition::staticConditions)
						.map(ctx::test)
						.orElse(true))
				.toList();

		if (!partCompositeAttrs.isEmpty()) {
			sources.computeIfAbsent(currentSource, k -> new ArrayList<>())
					.addAll(partCompositeAttrs);
		}

		return true; // Continue to children
	}

	@Override
	public void enterStructureSlot(OpenIdentifier slotId, Component component) {
		slotStack.push(slotId.toString());
	}

	@Override
	public void exitStructureSlot(OpenIdentifier slotId) {
		slotStack.pop();
	}

	@Override
	public void enterUpgradeSlot(ComponentUpgradeSlot slot) {
		inUpgradeSlot = true;
	}

	@Override
	public void exitUpgradeSlot(ComponentUpgradeSlot slot) {
		inUpgradeSlot = false;
	}

	/**
	 * Returns the collected attributes grouped by source slot.
	 *
	 * @return Map from source name (e.g., "self", "blade", "handle") to list of attributes
	 */
	@Override
	public Map<String, List<Attribute>> getResult() {
		return sources;
	}

	/**
	 * Returns the number of distinct sources that contributed attributes.
	 *
	 * @return Number of sources with at least one attribute
	 */
	public int getSourceCount() {
		return sources.size();
	}

	/**
	 * Checks if any attributes were collected.
	 *
	 * @return true if at least one attribute was collected from any source
	 */
	public boolean hasAttributes() {
		return !sources.isEmpty();
	}
}
