package com.sigmundgranaas.forgero.data.validation;

import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.component.api.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Context for validation, providing access to registries and other components.
 * This allows validators to check cross-references and resolve dependencies.
 */
public class ValidationContext {
	private final TagResolver tagResolver;
	private final Map<OpenIdentifier, CofComponent> allCofComponents;
	private final Map<OpenIdentifier, Component> builtComponents;
	private final Set<OpenIdentifier> knownSlotTypes;

	private ValidationContext(
			TagResolver tagResolver,
			Map<OpenIdentifier, CofComponent> allCofComponents,
			Map<OpenIdentifier, Component> builtComponents,
			Set<OpenIdentifier> knownSlotTypes
	) {
		this.tagResolver = tagResolver;
		this.allCofComponents = allCofComponents;
		this.builtComponents = builtComponents;
		this.knownSlotTypes = knownSlotTypes;
	}

	/**
	 * @return The tag resolver for checking tag existence and relationships.
	 */
	public TagResolver tagResolver() {
		return tagResolver;
	}

	/**
	 * Checks if a tag exists in the tag graph.
	 */
	public boolean tagExists(OpenIdentifier tag) {
		return tagResolver.getAllTags().contains(tag);
	}

	/**
	 * Checks if a component ID exists (as either a CofComponent or built Component).
	 */
	public boolean componentExists(OpenIdentifier id) {
		return allCofComponents.containsKey(id) || builtComponents.containsKey(id);
	}

	/**
	 * Gets a CofComponent by ID if it exists.
	 */
	public Optional<CofComponent> getCofComponent(OpenIdentifier id) {
		return Optional.ofNullable(allCofComponents.get(id));
	}

	/**
	 * Gets a built Component by ID if it exists.
	 */
	public Optional<Component> getComponent(OpenIdentifier id) {
		return Optional.ofNullable(builtComponents.get(id));
	}

	/**
	 * @return All CofComponent DTOs.
	 */
	public Map<OpenIdentifier, CofComponent> allCofComponents() {
		return allCofComponents;
	}

	/**
	 * @return All built runtime Components.
	 */
	public Map<OpenIdentifier, Component> builtComponents() {
		return builtComponents;
	}

	/**
	 * Checks if a slot type is known/valid.
	 */
	public boolean isKnownSlotType(OpenIdentifier slotType) {
		return knownSlotTypes.contains(slotType);
	}

	/**
	 * @return All known slot types.
	 */
	public Set<OpenIdentifier> knownSlotTypes() {
		return knownSlotTypes;
	}

	/**
	 * Builder for creating ValidationContext.
	 */
	public static class Builder {
		private TagResolver tagResolver;
		private Map<OpenIdentifier, CofComponent> allCofComponents = Map.of();
		private Map<OpenIdentifier, Component> builtComponents = Map.of();
		private Set<OpenIdentifier> knownSlotTypes = Set.of();

		public Builder tagResolver(TagResolver tagResolver) {
			this.tagResolver = tagResolver;
			return this;
		}

		public Builder allCofComponents(Map<OpenIdentifier, CofComponent> allCofComponents) {
			this.allCofComponents = allCofComponents;
			return this;
		}

		public Builder builtComponents(Map<OpenIdentifier, Component> builtComponents) {
			this.builtComponents = builtComponents;
			return this;
		}

		public Builder knownSlotTypes(Set<OpenIdentifier> knownSlotTypes) {
			this.knownSlotTypes = knownSlotTypes;
			return this;
		}

		public ValidationContext build() {
			if (tagResolver == null) {
				throw new IllegalStateException("TagResolver is required");
			}
			return new ValidationContext(tagResolver, allCofComponents, builtComponents, knownSlotTypes);
		}
	}
}
