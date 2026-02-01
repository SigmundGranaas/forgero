package com.sigmundgranaas.forgero.core.attribute.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ComponentTreeVisitor;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;

import java.util.ArrayList;
import java.util.List;

import static com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY;

/**
 * Collects attributes with no scope (default propagation) from structure parts.
 *
 * <p>Default attributes (those with no explicit scope) propagate normally through
 * the component hierarchy without any special composition handling. This collector
 * gathers these attributes from the structure tree.</p>
 *
 * <h2>Collection Rules</h2>
 * <ul>
 *   <li>Only collects attributes with NO scope (scope is empty)</li>
 *   <li>Applies static condition filtering</li>
 *   <li>Does NOT traverse into upgrade slots (they have separate handling)</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * DefaultAttributeCollector collector = new DefaultAttributeCollector();
 * ComponentTreeWalker.walk(component, collector);
 * List<Attribute> defaults = collector.getResult();
 * }</pre>
 */
public class DefaultAttributeCollector implements ComponentTreeVisitor<List<Attribute>> {

	private final List<Attribute> result = new ArrayList<>();
	private boolean inUpgradeSlot = false;

	@Override
	public boolean visit(Component component, Component root, int depth) {
		// Don't collect from upgrade slots - they have separate handling
		if (inUpgradeSlot) {
			return false;
		}

		ResolutionContext ctx = new ResolutionContext(component, root);

		List<Attribute> defaults = component.properties(KEY).stream()
				.filter(attr -> attr.scope().isEmpty())
				.filter(attr -> attr.condition()
						.map(Condition::staticConditions)
						.map(ctx::test)
						.orElse(true))
				.toList();

		result.addAll(defaults);
		return true; // Continue to children
	}

	@Override
	public void enterStructureSlot(OpenIdentifier slotId, Component component) {
		// No special handling needed
	}

	@Override
	public void exitStructureSlot(OpenIdentifier slotId) {
		// No special handling needed
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
	 * Returns all collected default (no-scope) attributes.
	 *
	 * @return List of attributes with no scope
	 */
	@Override
	public List<Attribute> getResult() {
		return result;
	}

	/**
	 * Returns the number of collected attributes.
	 *
	 * @return Count of collected attributes
	 */
	public int getCount() {
		return result.size();
	}

	/**
	 * Checks if any attributes were collected.
	 *
	 * @return true if at least one attribute was collected
	 */
	public boolean hasAttributes() {
		return !result.isEmpty();
	}
}
