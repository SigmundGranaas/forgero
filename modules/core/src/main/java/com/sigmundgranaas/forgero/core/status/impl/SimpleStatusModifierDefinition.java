package com.sigmundgranaas.forgero.core.status.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.status.api.StatusModifier;
import com.sigmundgranaas.forgero.core.status.api.StatusModifierDefinition;

import java.util.*;

/**
 * Default implementation of StatusModifierDefinition.
 * Combines a StatusModifier with its targeting rules and occurrence chance.
 *
 * @param modifier    The status modifier this definition describes
 * @param targetTypes Component types this modifier can apply to
 * @param targetIds   Specific component IDs this modifier can apply to
 * @param chance      Probability of natural occurrence (0.0-1.0)
 */
public record SimpleStatusModifierDefinition(
		StatusModifier modifier,
		Set<OpenIdentifier> targetTypes,
		Set<OpenIdentifier> targetIds,
		float chance
) implements StatusModifierDefinition {

	/**
	 * Canonical constructor with defensive copies and validation.
	 */
	public SimpleStatusModifierDefinition {
		Objects.requireNonNull(modifier, "modifier cannot be null");
		targetTypes = targetTypes != null ? Set.copyOf(targetTypes) : Set.of();
		targetIds = targetIds != null ? Set.copyOf(targetIds) : Set.of();
		if (chance < 0 || chance > 1) {
			throw new IllegalArgumentException("chance must be between 0.0 and 1.0, got: " + chance);
		}
	}

	// Convenience factory methods

	/**
	 * Creates a definition with no targeting restrictions.
	 */
	public static SimpleStatusModifierDefinition universal(StatusModifier modifier) {
		return new SimpleStatusModifierDefinition(modifier, Set.of(), Set.of(), 0f);
	}

	/**
	 * Creates a definition with type restrictions.
	 */
	public static SimpleStatusModifierDefinition forTypes(
			StatusModifier modifier,
			Set<OpenIdentifier> targetTypes
	) {
		return new SimpleStatusModifierDefinition(modifier, targetTypes, Set.of(), 0f);
	}

	/**
	 * Creates a definition with type restrictions and chance.
	 */
	public static SimpleStatusModifierDefinition forTypesWithChance(
			StatusModifier modifier,
			Set<OpenIdentifier> targetTypes,
			float chance
	) {
		return new SimpleStatusModifierDefinition(modifier, targetTypes, Set.of(), chance);
	}

	/**
	 * Creates a definition for specific component IDs only.
	 */
	public static SimpleStatusModifierDefinition forIds(
			StatusModifier modifier,
			Set<OpenIdentifier> targetIds
	) {
		return new SimpleStatusModifierDefinition(modifier, Set.of(), targetIds, 0f);
	}

	// Builder pattern for complex construction

	/**
	 * Creates a builder for a SimpleStatusModifierDefinition.
	 *
	 * @param modifier The modifier this definition describes
	 * @return A new builder instance
	 */
	public static Builder builder(StatusModifier modifier) {
		return new Builder(modifier);
	}

	/**
	 * Builder for constructing SimpleStatusModifierDefinition instances.
	 */
	public static final class Builder {
		private final StatusModifier modifier;
		private final Set<OpenIdentifier> targetTypes = new HashSet<>();
		private final Set<OpenIdentifier> targetIds = new HashSet<>();
		private float chance = 0f;

		private Builder(StatusModifier modifier) {
			this.modifier = modifier;
		}

		public Builder targetType(OpenIdentifier type) {
			targetTypes.add(type);
			return this;
		}

		public Builder targetTypes(OpenIdentifier... types) {
			targetTypes.addAll(Arrays.asList(types));
			return this;
		}

		public Builder targetTypes(Collection<OpenIdentifier> types) {
			targetTypes.addAll(types);
			return this;
		}

		public Builder targetId(OpenIdentifier id) {
			targetIds.add(id);
			return this;
		}

		public Builder targetIds(OpenIdentifier... ids) {
			targetIds.addAll(Arrays.asList(ids));
			return this;
		}

		public Builder targetIds(Collection<OpenIdentifier> ids) {
			targetIds.addAll(ids);
			return this;
		}

		public Builder chance(float chance) {
			this.chance = chance;
			return this;
		}

		public SimpleStatusModifierDefinition build() {
			return new SimpleStatusModifierDefinition(modifier, targetTypes, targetIds, chance);
		}
	}
}
