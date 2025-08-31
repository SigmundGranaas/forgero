package com.sigmundgranaas.forgero.common.identifier.api;

public record IdentifierFactory( String defaultNamespace) {

	/**
	 * Creates a canonical OpenIdentifier from a string.
	 * If the string contains a ':', it is treated as "namespace:path".
	 * Otherwise, the configured default namespace is used.
	 * The path part is always normalized to a single-group name (stripping directories and file extension).
	 *
	 * @param identifier The string to parse (e.g., "forgero:materials/iron.json" or "iron_pickaxe_head").
	 * @return A new OpenIdentifier instance representing the canonical ID (e.g., "forgero:iron" or "forgero:iron_pickaxe_head").
	 */
	public OpenIdentifier of(String identifier) {
		String[] parts = identifier.split(":", 2);
		OpenIdentifier tempId;
		if (parts.length == 2) {
			tempId = new OpenIdentifier(parts[0], parts[1]);
		} else {
			tempId = new OpenIdentifier(this.defaultNamespace, parts[0]);
		}
		return tempId.toCanonical(); // Always return the canonical form
	}

	/**
	 * Creates a canonical OpenIdentifier from an explicit namespace and path.
	 * The path is normalized to a single-group name (stripping directories and file extension).
	 *
	 * @param namespace The namespace for the identifier.
	 * @param path      The raw path for the identifier (e.g., "materials/iron.json" or "iron_pickaxe_head").
	 * @return A new OpenIdentifier instance representing the canonical ID (e.g., "forgero:iron" or "forgero:iron_pickaxe_head").
	 */
	public OpenIdentifier of(String namespace, String path) {
		// Construct a temporary OpenIdentifier with the raw path, then convert to canonical.
		return new OpenIdentifier(namespace, path).toCanonical();
	}

	public static class Builder {
		private String defaultNamespace = "forgero";

		/**
		 * Sets the default namespace to be used for identifiers that do not specify one.
		 *
		 * @param namespace The default namespace.
		 * @return this builder.
		 */
		public Builder defaultNamespace(String namespace) {
			this.defaultNamespace = namespace;
			return this;
		}

		/**
		 * Builds the configured, immutable IdentifierFactory.
		 *
		 * @return A new IdentifierFactory.
		 */
		public IdentifierFactory build() {
			return new IdentifierFactory(this.defaultNamespace);
		}
	}
}
