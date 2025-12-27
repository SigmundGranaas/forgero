package com.sigmundgranaas.forgero.data.pipeline.util;

import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.data.loading.api.RawDefinition;
import com.sigmundgranaas.forgero.data.loading.api.data.ResourceTypeData;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves ID template strings by replacing placeholders with values from components.
 * <p>
 * Template strings use placeholder syntax: {@code {slot.property}} or {@code {slot.nested.property}}
 * <p>
 * Examples:
 * <ul>
 *   <li>{@code {material.name}-{shape.name}} → {@code iron-pickaxe_head}</li>
 *   <li>{@code {head.material.name}-tool} → {@code iron-tool}</li>
 *   <li>{@code {shape.shape_name}} → {@code pickaxe} (strips _shape suffix)</li>
 * </ul>
 * <p>
 * This class is reusable across template generation, host data generation, and any other
 * context that needs to resolve template strings from component combinations.
 *
 * @since 0.14.0
 */
public class IdTemplateResolver {

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");

	private final IdentifierFactory idFactory;
	private final Map<OpenIdentifier, RawDefinition> rawDefinitions;

	/**
	 * Constructs a new ID template resolver.
	 *
	 * @param idFactory      Factory for creating OpenIdentifiers when traversing structure
	 * @param rawDefinitions Map of raw definitions for accessing component metadata
	 */
	public IdTemplateResolver(IdentifierFactory idFactory, Map<OpenIdentifier, RawDefinition> rawDefinitions) {
		this.idFactory = idFactory;
		this.rawDefinitions = rawDefinitions;
	}

	/**
	 * Resolves all placeholders in a template string using the provided component combination.
	 * <p>
	 * Placeholders that cannot be resolved (missing components, invalid paths) are left unchanged.
	 *
	 * @param template    The template string containing placeholders
	 * @param combination Map of slot names to components
	 * @return The template string with all resolvable placeholders replaced
	 */
	public String resolve(String template, Map<String, CofComponent> combination) {
		Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
		return matcher.replaceAll(matchResult -> {
			String placeholder = matchResult.group(1);
			String resolved = resolvePlaceholder(placeholder, combination);
			// If resolution failed, keep the original placeholder
			return resolved != null ? resolved : matchResult.group(0);
		});
	}

	/**
	 * Resolves a single placeholder to its value.
	 *
	 * @param placeholder The placeholder without braces (e.g., "material.name" or "head.material.name")
	 * @param combination Map of slot names to components
	 * @return The resolved value, or null if resolution failed
	 */
	private String resolvePlaceholder(String placeholder, Map<String, CofComponent> combination) {
		String[] parts = placeholder.split("\\.");
		if (parts.length < 2) {
			return null; // Invalid placeholder format
		}

		// Get the component in the named slot
		CofComponent componentInSlot = combination.get(parts[0]);
		if (componentInSlot == null) {
			return null; // Slot not found in combination
		}

		// Handle nested property access (e.g., "head.material.name")
		if (parts.length > 2) {
			return resolveNestedProperty(parts, componentInSlot);
		}

		// Handle direct property access (e.g., "material.name", "shape.shape_name")
		return resolveSimpleProperty(parts[0], parts[1], componentInSlot);
	}

	/**
	 * Resolves nested property access by traversing component structure.
	 *
	 * @param parts             Array of path parts (e.g., ["head", "material", "name"])
	 * @param componentInSlot   The root component to start traversal from
	 * @return The resolved value, or null if traversal failed
	 */
	private String resolveNestedProperty(String[] parts, CofComponent componentInSlot) {
		CofComponent current = componentInSlot;

		// Traverse through structure slots (skip first part - slot name, and last part - property name)
		for (int i = 1; i < parts.length - 1; i++) {
			if (current.structure() == null) {
				return null; // No structure to traverse
			}

			OpenIdentifier slotId = idFactory.of(parts[i]);
			if (!current.structure().slots().containsKey(slotId)) {
				return null; // Slot not found in structure
			}

			current = current.structure().slots().get(slotId).content();
			if (current == null) {
				return null; // Empty slot
			}
		}

		// Resolve the final property
		String property = parts[parts.length - 1];
		if ("name".equals(property)) {
			String componentName = current.id().name();

			// Special case: strip _shape suffix if the slot we just accessed is "shape"
			// e.g., {head.shape.name} where parts[parts.length - 2] == "shape"
			if (parts.length > 2 && "shape".equals(parts[parts.length - 2]) && componentName.endsWith("_shape")) {
				return componentName.substring(0, componentName.length() - "_shape".length());
			}

			return componentName;
		}

		return null; // Unknown property
	}

	/**
	 * Resolves a simple property access (non-nested).
	 *
	 * @param slotName  The slot name (e.g., "material", "shape")
	 * @param property  The property name (e.g., "name", "shape_name")
	 * @param component The component to get the property from
	 * @return The resolved value, or null if resolution failed
	 */
	private String resolveSimpleProperty(String slotName, String property, CofComponent component) {
		// Handle special {shape.shape_name} placeholder
		if ("shape_name".equals(property)) {
			return resolveShapeName(component);
		}

		// Handle {*.name} placeholder
		if ("name".equals(property)) {
			String componentName = component.id().name();

			// Special case: strip _shape suffix from shape names
			if ("shape".equals(slotName) && componentName.endsWith("_shape")) {
				return componentName.substring(0, componentName.length() - "_shape".length());
			}

			return componentName;
		}

		return null; // Unknown property
	}

	/**
	 * Resolves the shape_name property, which strips the "_shape" suffix if present.
	 * <p>
	 * This checks the raw definition's includes first, falling back to the component's own name.
	 *
	 * @param component The shape component
	 * @return The shape name without "_shape" suffix
	 */
	private String resolveShapeName(CofComponent component) {
		RawDefinition rawDef = rawDefinitions.get(component.id());

		// Try to get name from includes (for inherited shapes)
		if (rawDef != null && rawDef.data() instanceof ResourceTypeData resourceTypeData) {
			if (resourceTypeData.include() != null && !resourceTypeData.include().isEmpty()) {
				String includeName = resourceTypeData.include().get(0).name();
				return stripShapeSuffix(includeName);
			}
		}

		// Fall back to component's own name
		return stripShapeSuffix(component.id().name());
	}

	/**
	 * Strips the "_shape" suffix from a name if present.
	 */
	private String stripShapeSuffix(String name) {
		if (name.endsWith("_shape")) {
			return name.substring(0, name.length() - "_shape".length());
		}
		return name;
	}
}
