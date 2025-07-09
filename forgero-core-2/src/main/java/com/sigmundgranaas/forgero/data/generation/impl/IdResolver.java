package com.sigmundgranaas.forgero.data.generation.impl;

import com.sigmundgranaas.forgero.data.generation.api.GeneratedState;
import com.sigmundgranaas.forgero.data.processing.api.NormalizedState;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A dedicated class for resolving templated IDs from patterns like "forgero:{head.material.name}-pickaxe".
 * It traverses a context object graph (which can include NormalizedState and GeneratedState DTOs)
 * to replace placeholders with canonical ID-friendly strings (lowercase, underscores).
 */
public class IdResolver {

	private final NormalizedState normalizedState;
	private final Map<OpenIdentifier, GeneratedState.GeneratedPart> generatedParts;

	/**
	 * Constructs an IdResolver.
	 *
	 * @param normalizedState The overall normalized state, used to look up basic materials, shapes, and static parts.
	 * @param generatedParts  A map of already generated parts, crucial for resolving nested parts within tools.
	 */
	public IdResolver(NormalizedState normalizedState, Map<OpenIdentifier, GeneratedState.GeneratedPart> generatedParts) {
		this.normalizedState = normalizedState;
		this.generatedParts = generatedParts;
	}

	/**
	 * Returns an unmodifiable view of the generated parts this resolver knows about.
	 */
	public Map<OpenIdentifier, GeneratedState.GeneratedPart> getGeneratedParts() {
		return Collections.unmodifiableMap(generatedParts);
	}

	/**
	 * Resolves a templated ID pattern string using provided context.
	 * The pattern can contain placeholders like {key.nestedKey}.
	 * Resolved values are converted to lowercase and spaces/special characters are replaced with underscores.
	 *
	 * @param idPattern The ID pattern string (e.g., "forgero:{material.name}-{shape.name}").
	 * @param context   A map where keys are top-level placeholder names (e.g., "material", "shape", "head", "handle")
	 *                  and values are the corresponding DTOs or OpenIdentifiers. This map can be modified during traversal.
	 * @return The resolved ID string with placeholders replaced and sanitized for ID use.
	 */
	public String resolveId(String idPattern, Map<String, Object> context) {
		if (idPattern == null || idPattern.isEmpty()) {
			return "";
		}

		Pattern pattern = Pattern.compile("\\{([^}]+)}"); // Matches {path.to.value}
		Matcher matcher = pattern.matcher(idPattern);
		StringBuilder sb = new StringBuilder();

		while (matcher.find()) {
			String fullPath = matcher.group(1); // e.g., "material.name" or "head.material.name"
			Object resolvedValue = traversePath(context, fullPath);

			String replacement = "";
			if (resolvedValue != null) {
				// For IDs, we want the raw path if it's an OpenIdentifier, otherwise the sanitized name.
				if (resolvedValue instanceof OpenIdentifier oid) {
					replacement = oid.path(); // Canonical path of the ID
				} else if (resolvedValue instanceof String str) {
					replacement = str;
				} else if (resolvedValue instanceof NormalizedState.NormalizedMaterial nm) {
					replacement = nm.name();
				} else if (resolvedValue instanceof NormalizedState.NormalizedShape ns) {
					replacement = ns.name();
				} else if (resolvedValue instanceof NormalizedState.NormalizedStaticPart nsp) {
					replacement = nsp.name();
				} else if (resolvedValue instanceof GeneratedState.GeneratedPart gp) {
					replacement = gp.id().name();
				} else {
					replacement = String.valueOf(resolvedValue);
				}
				// Sanitize the replacement for ID use (lowercase, replace spaces/special chars with underscores)
				replacement = replacement.toLowerCase().replaceAll("[^a-z0-9_]+", "_");
			}
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * Traverses the object graph based on a dot-separated path.
	 *
	 * @param root    The starting object (typically a Map for the initial context).
	 * @param fullPath The dot-separated path (e.g., "head.material.name").
	 * @return The value found at the end of the path, or null if any part of the path is invalid.
	 */
	private @Nullable Object traversePath(Object root, String fullPath) {
		Object current = root;
		String[] pathParts = fullPath.split("\\.");

		for (int i = 0; i < pathParts.length; i++) {
			String part = pathParts[i];

			if (current == null) return null;

			if (current instanceof Map<?, ?> mapContext) {
				current = mapContext.get(part);
			} else {
				current = getProperty(current, part);
			}

			// If we hit an OpenIdentifier in the middle of a path (e.g., 'head' yields an OpenIdentifier),
			// try to resolve it to a concrete DTO to continue traversal.
			if (current instanceof OpenIdentifier oid && i < pathParts.length - 1) {
				current = resolveComponentFromId(oid);
			}
		}
		return current;
	}

	/**
	 * Helper to get a direct property from a known DTO.
	 * This method needs to be extended if new DTOs are introduced with properties to be templated.
	 *
	 * @param obj          The DTO object.
	 * @param propertyName The name of the property to retrieve (e.g., "name", "material").
	 * @return The value of the property, or null if not found.
	 */
	private @Nullable Object getProperty(Object obj, String propertyName) {
		if (obj == null) return null;

		if (obj instanceof NormalizedState.NormalizedMaterial material) {
			return switch (propertyName) {
				case "name" -> material.name();
				case "id" -> material.id();
				default -> null;
			};
		} else if (obj instanceof NormalizedState.NormalizedShape shape) {
			return switch (propertyName) {
				case "name" -> shape.name();
				case "id" -> shape.id();
				default -> null;
			};
		} else if (obj instanceof NormalizedState.NormalizedStaticPart staticPart) {
			return switch (propertyName) {
				case "name" -> staticPart.name();
				case "id" -> staticPart.id();
				default -> null;
			};
		} else if (obj instanceof GeneratedState.GeneratedPart generatedPart) {
			return switch (propertyName) {
				case "name" -> generatedPart.id().name();
				case "id" -> generatedPart.id();
				case "material" -> generatedPart.materialId();
				case "shape" -> generatedPart.shapeId();
				default -> null;
			};
		} else if (obj instanceof NormalizedState.NormalizedPartTemplate partTemplate) {
			return switch (propertyName) {
				case "name" -> partTemplate.name();
				case "id" -> partTemplate.id();
				default -> null;
			};
		}
		if (obj instanceof String) {
			return obj;
		}

		return null;
	}

	/**
	 * Resolves an OpenIdentifier to its corresponding NormalizedState or GeneratedState DTO.
	 * This is crucial for traversing object graphs that contain IDs instead of direct DTOs.
	 */
	public @Nullable Object resolveComponentFromId(OpenIdentifier id) {
		if (id == null) return null;

		if (generatedParts.containsKey(id)) {
			return generatedParts.get(id);
		}
		if (normalizedState.staticParts().containsKey(id)) {
			return normalizedState.staticParts().get(id);
		}
		if (normalizedState.materials().containsKey(id)) {
			return normalizedState.materials().get(id);
		}
		if (normalizedState.shapes().containsKey(id)) {
			return normalizedState.shapes().get(id);
		}
		return null; // ID not resolved to a known DTO
	}
}
