package com.sigmundgranaas.forgero.core.property.v2.attribute.attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The AttributeModificationRegistry provides a mechanism to register and retrieve modifications for attributes.
 * Modifications allow custom behavior to be applied to attributes, like handling broken states, or clamping values.
 *
 * <p>Example usage for registering a single modification:
 * <pre>
 *     AttributeModificationRegistry.modificationBuilder()
 *                                  .attributeKey(AttackDamage.KEY)
 *                                  .modification(new MyCustomModification())
 *                                  .register();
 * </pre>
 *
 * <p>Example usage for registering multiple modifications for a single attribute:
 * <pre>
 *     AttributeModificationRegistry.modificationBuilder()
 *                                  .attributeKey(AttackSpeed.KEY)
 *                                  .modifications(List.of(modification1, modification2))
 *                                  .register();
 * </pre>
 */
public class AttributeModificationRegistry {

	/**
	 * Map holding registered modifications against their attribute keys.
	 */
	private static final Map<String, List<AttributeModification>> MODIFICATIONS = new ConcurrentHashMap<>();

	private AttributeModificationRegistry() {
	}

	/**
	 * Provides a new instance of {@link ModificationBuilder} for fluent API registration.
	 *
	 * @return a new ModificationBuilder instance
	 */
	public static ModificationBuilder modificationBuilder() {
		return new ModificationBuilder();
	}

	/**
	 * Retrieves the list of registered modifications for a given attribute key.
	 *
	 * @param attribute the attribute key for which modifications are requested
	 * @return a list of modifications for the attribute; empty if none are found
	 */
	public static List<AttributeModification> getModifications(String attribute) {
		return MODIFICATIONS.getOrDefault(attribute, Collections.emptyList());
	}

	public static final class ModificationBuilder {
		private final List<AttributeModification> modifications = new ArrayList<>();
		private String attributeKey;

		private ModificationBuilder() {
		}

		/**
		 * Sets the attribute key for which modifications are being registered.
		 *
		 * @param attributeKey the key of the attribute
		 * @return the current ModificationBuilder instance for chaining
		 */
		public ModificationBuilder attributeKey(String attributeKey) {
			this.attributeKey = attributeKey;
			return this;
		}

		/**
		 * Adds a single modification to the current registration.
		 *
		 * @param modification the modification to add
		 * @return the current ModificationBuilder instance for chaining
		 */
		public ModificationBuilder modification(AttributeModification modification) {
			modifications.add(modification);
			return this;
		}

		/**
		 * Adds multiple modifications to the current registration.
		 *
		 * @param mods a list of modifications to add
		 * @return the current ModificationBuilder instance for chaining
		 */
		public ModificationBuilder modifications(List<AttributeModification> mods) {
			this.modifications.addAll(mods);
			return this;
		}

		/**
		 * Finalizes the registration of modifications for the attribute key.
		 * If an attribute key already has registered modifications, new modifications will be appended.
		 */
		public void register() {
			if (attributeKey == null || modifications.isEmpty()) {
				throw new IllegalStateException("Attribute key and at least one modification must be set before registering");
			}
			MODIFICATIONS.merge(attributeKey, modifications, (existing, newMods) -> {
				existing.addAll(newMods);
				return existing;
			});
		}
	}
}
