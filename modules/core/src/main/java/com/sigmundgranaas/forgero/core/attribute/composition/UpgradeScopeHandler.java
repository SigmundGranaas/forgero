package com.sigmundgranaas.forgero.core.attribute.composition;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeScope;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles attributes with the "upgrade" scope.
 *
 * <p>Upgrade attributes only apply when the component is installed in an upgrade slot.
 * They are filtered out when the component is used as a primary material or part.</p>
 *
 * <h3>Behavior:</h3>
 * <ul>
 *   <li>Only includes attributes when source indicates upgrade slot placement</li>
 *   <li>Filters out attributes when component is used as primary material</li>
 *   <li>Resolved attributes have no scope (already processed)</li>
 * </ul>
 *
 * <h3>Use Case:</h3>
 * <p>A gem might have bonus damage that only applies when socketed into a tool,
 * not when used as a crafting ingredient.</p>
 *
 * <h3>Source Naming Convention:</h3>
 * <p>Sources with names starting with "upgrade:" are considered upgrade slots.
 * For example: "upgrade:gem_slot", "upgrade:reinforcement"</p>
 */
public class UpgradeScopeHandler implements AttributeScopeHandler {

	public static final UpgradeScopeHandler INSTANCE = new UpgradeScopeHandler();

	/**
	 * Prefix for source names that indicate an upgrade slot.
	 */
	public static final String UPGRADE_SOURCE_PREFIX = "upgrade:";

	@Override
	public OpenIdentifier scopeId() {
		return AttributeScope.UPGRADE;
	}

	@Override
	public List<Attribute> compose(Map<String, List<Attribute>> sources) {
		List<Attribute> result = new ArrayList<>();

		for (var entry : sources.entrySet()) {
			String sourceName = entry.getKey();

			// Only include attributes from upgrade sources
			if (isUpgradeSource(sourceName)) {
				for (Attribute attr : entry.getValue()) {
					result.add(SimpleAttribute.resolved(attr.type(), attr.value()));
				}
			}
		}

		return result;
	}

	/**
	 * Checks if the source name indicates an upgrade slot.
	 *
	 * @param sourceName The source name to check
	 * @return true if this is an upgrade source
	 */
	private boolean isUpgradeSource(String sourceName) {
		return sourceName != null && sourceName.startsWith(UPGRADE_SOURCE_PREFIX);
	}
}
