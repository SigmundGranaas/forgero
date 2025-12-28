package com.sigmundgranaas.forgero.core.component.api.slot;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.data.loading.impl.codec.CodecConstants;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Validates whether a component can be placed in a slot.
 * This is a value object - validation rules are data, not behavior to be extended.
 * <p>
 * Combines tag-based validation with custom predicate validation.
 */
public record SlotValidator(
		OpenIdentifier requiredType,
		Predicate<Component> customValidator
) {
	/**
	 * A validator that accepts any component.
	 */
	public static final SlotValidator ACCEPT_ALL = new SlotValidator(null, c -> true);

	/**
	 * Codec for SlotValidator.
	 * Only serializes the requiredType tag - custom predicates are not serializable
	 * and default to accepting all components on deserialization.
	 */
	public static final Codec<SlotValidator> CODEC = CodecConstants.OPEN_IDENTIFIER_CODEC
			.optionalFieldOf("required_tag")
			.xmap(
					opt -> opt.map(SlotValidator::requireTag).orElse(ACCEPT_ALL),
					validator -> Optional.ofNullable(validator.requiredType())
			).codec();

	/**
	 * Creates a validator that requires the component to have a specific tag.
	 */
	public static SlotValidator requireTag(OpenIdentifier tag) {
		return new SlotValidator(tag, c -> true);
	}

	/**
	 * Creates a validator that requires the component to have all of the specified tags.
	 */
	public static SlotValidator requireAllTags(Collection<OpenIdentifier> tags) {
		if (tags == null || tags.isEmpty()) {
			return ACCEPT_ALL;
		}
		Set<OpenIdentifier> tagSet = tags instanceof Set ? (Set<OpenIdentifier>) tags : new HashSet<>(tags);
		return new SlotValidator(null, c -> c.getTags().containsAll(tagSet));
	}

	/**
	 * Creates a validator with only a custom predicate.
	 */
	public static SlotValidator custom(Predicate<Component> predicate) {
		return new SlotValidator(null, predicate);
	}

	/**
	 * Creates a validator requiring both a tag and a custom condition.
	 */
	public static SlotValidator requireTagAnd(OpenIdentifier tag, Predicate<Component> predicate) {
		return new SlotValidator(tag, predicate);
	}

	/**
	 * Tests if a component is valid for this slot.
	 *
	 * @param component The component to validate
	 * @return true if the component passes all validation rules
	 */
	public boolean test(Component component) {
		if (component == null) {
			return false;
		}
		if (requiredType != null && !component.getTags().contains(requiredType)) {
			return false;
		}
		return customValidator.test(component);
	}

	/**
	 * Returns a validation error message, or empty if valid.
	 *
	 * @param component The component to validate
	 * @param slotId    The slot ID for error messaging
	 * @return Error message if invalid, empty if valid
	 */
	public Optional<String> validate(Component component, OpenIdentifier slotId) {
		if (component == null) {
			return Optional.of(String.format("Cannot place null component in slot '%s'", slotId));
		}
		if (requiredType != null && !component.getTags().contains(requiredType)) {
			return Optional.of(String.format(
					"Component '%s' missing required tag '%s' for slot '%s'",
					component.id(), requiredType, slotId));
		}
		if (!customValidator.test(component)) {
			return Optional.of(String.format(
					"Component '%s' failed custom validation for slot '%s'",
					component.id(), slotId));
		}
		return Optional.empty();
	}

	/**
	 * Combines this validator with another, requiring both to pass.
	 */
	public SlotValidator and(SlotValidator other) {
		OpenIdentifier combinedType = this.requiredType != null ? this.requiredType : other.requiredType;
		Predicate<Component> combinedPredicate = c -> this.customValidator.test(c) && other.customValidator.test(c);

		// If both have required types, we need to check both
		if (this.requiredType != null && other.requiredType != null) {
			Predicate<Component> bothTags = c ->
					c.getTags().contains(this.requiredType) && c.getTags().contains(other.requiredType);
			return new SlotValidator(null, bothTags.and(combinedPredicate));
		}

		return new SlotValidator(combinedType, combinedPredicate);
	}

	@Override
	public String toString() {
		if (requiredType != null) {
			return "SlotValidator{requiresTag=" + requiredType + "}";
		}
		return "SlotValidator{custom}";
	}
}
