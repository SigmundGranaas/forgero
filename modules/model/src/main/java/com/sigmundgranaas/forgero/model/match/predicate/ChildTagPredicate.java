package com.sigmundgranaas.forgero.model.match.predicate;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.model.api.ModelResolutionContext;
import com.sigmundgranaas.forgero.model.match.Predicate;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A predicate that tests if the current component being resolved ('aComponent' in the context)
 * has a specific tag. This is used in slot renderers to apply different models based on the
 * type of component being inserted into the slot.
 * <p>
 * Supports both direct tag matching and inheritance-aware matching via TagResolver.
 */
public final class ChildTagPredicate implements Predicate {
	private final OpenIdentifier tag;
	private final transient Supplier<TagResolver> resolverSupplier;

	/**
	 * Creates a predicate with inheritance-aware matching via the provided resolver.
	 */
	public ChildTagPredicate(OpenIdentifier tag, Supplier<TagResolver> resolverSupplier) {
		this.tag = Objects.requireNonNull(tag, "tag cannot be null");
		this.resolverSupplier = resolverSupplier;
	}

	/**
	 * Creates a predicate with direct matching only (no inheritance).
	 */
	public ChildTagPredicate(OpenIdentifier tag) {
		this(tag, null);
	}

	public OpenIdentifier tag() {
		return tag;
	}

	@Override
	public boolean test(ModelResolutionContext context) {
		if (resolverSupplier != null) {
			TagResolver resolver = resolverSupplier.get();
			if (resolver != null) {
				// 'aComponent' is the component for which the model is currently being resolved.
				// In the context of a slot, this will be the child component.
				return resolver.hasTag(context.aComponent(), tag);
			}
		}
		// Fallback to direct matching
		return context.aComponent().getTags().contains(tag);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ChildTagPredicate that)) return false;
		return Objects.equals(tag, that.tag);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tag);
	}

	@Override
	public String toString() {
		return "ChildTagPredicate[tag=" + tag + "]";
	}
}
