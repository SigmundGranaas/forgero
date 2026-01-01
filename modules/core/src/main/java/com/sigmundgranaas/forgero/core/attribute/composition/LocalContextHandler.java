package com.sigmundgranaas.forgero.core.attribute.composition;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeContext;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles attributes with the "local" context.
 *
 * <p>Local attributes only apply to the component they're defined on.
 * They do NOT propagate to parent components during composition.</p>
 *
 * <h3>Behavior:</h3>
 * <ul>
 *   <li>Only includes attributes from the "self" source</li>
 *   <li>Filters out attributes from child components</li>
 *   <li>Resolved attributes have no context (already processed)</li>
 * </ul>
 *
 * <h3>Use Case:</h3>
 * <p>A material might have a "crafting_speed" bonus that should only apply
 * when working with that material directly, not when the material is part
 * of a finished tool.</p>
 */
public class LocalContextHandler implements AttributeContextHandler {

	public static final LocalContextHandler INSTANCE = new LocalContextHandler();

	/**
	 * The source name used for the component being directly queried.
	 */
	public static final String SELF_SOURCE = "self";

	@Override
	public OpenIdentifier contextId() {
		return AttributeContext.LOCAL;
	}

	@Override
	public List<Attribute> compose(Map<String, List<Attribute>> sources) {
		// Only include attributes from "self" - the component being directly queried
		List<Attribute> selfAttributes = sources.get(SELF_SOURCE);

		if (selfAttributes == null || selfAttributes.isEmpty()) {
			return List.of();
		}

		// Convert to resolved attributes (remove context since they're now processed)
		List<Attribute> result = new ArrayList<>();
		for (Attribute attr : selfAttributes) {
			result.add(SimpleAttribute.resolved(attr.type(), attr.value()));
		}

		return result;
	}
}
