package com.sigmundgranaas.forgero.core.identifier.api;

public class IdentifierFactory {

	private final String defaultNamespace;

	private IdentifierFactory(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
	}

	/**
	 * Creates an OpenIdentifier from a string.
	 * If the string contains a ':', it is treated as "namespace:path".
	 * Otherwise, the configured default namespace is used.
	 *
	 * @param identifier The string to parse.
	 * @return A new OpenIdentifier instance.
	 */
	public OpenIdentifier of(String identifier) {
		String[] parts = identifier.split(":", 2);
		if (parts.length == 2) {
			return new OpenIdentifier(parts[0], parts[1]);
		}
		return new OpenIdentifier(this.defaultNamespace, parts[0]);
	}

	/**
	 * Creates an OpenIdentifier from an explicit namespace and path.
	 *
	 * @param namespace The namespace for the identifier.
	 * @param path      The path for the identifier.
	 * @return A new OpenIdentifier instance.
	 */
	public OpenIdentifier of(String namespace, String path) {
		return new OpenIdentifier(namespace, path);
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
