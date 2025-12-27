package com.sigmundgranaas.forgero.model.match.predicate;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.model.api.ModelResolutionContext;
import com.sigmundgranaas.forgero.model.match.Predicate;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A predicate that tests if the root component has a specific tag.
 * Supports both direct tag matching and inheritance-aware matching via TagResolver.
 */
public final class RootTagPredicate implements Predicate {
	private final OpenIdentifier tag;
	private final transient Supplier<TagResolver> resolverSupplier;

	/**
	 * Creates a predicate with inheritance-aware matching via the provided resolver.
	 */
	public RootTagPredicate(OpenIdentifier tag, Supplier<TagResolver> resolverSupplier) {
		this.tag = Objects.requireNonNull(tag, "tag cannot be null");
		this.resolverSupplier = resolverSupplier;
	}

	/**
	 * Creates a predicate with direct matching only (no inheritance).
	 */
	public RootTagPredicate(OpenIdentifier tag) {
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
				return resolver.hasTag(context.root(), tag);
			}
		}
		// Fallback to direct matching
		return context.root().getTags().contains(tag);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RootTagPredicate that)) return false;
		return Objects.equals(tag, that.tag);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tag);
	}

	@Override
	public String toString() {
		return "RootTagPredicate[tag=" + tag + "]";
	}
}
