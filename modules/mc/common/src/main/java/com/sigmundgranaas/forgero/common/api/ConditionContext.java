package com.sigmundgranaas.forgero.common.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;

import java.util.Optional;
import java.util.Set;

/**
 * The public, read-only view a custom condition is evaluated against — the structural facts of
 * <em>where</em> a component sits in its assembly, exposed without any internal type.
 *
 * <p>Authors register a condition as a {@code Predicate<ConditionContext>} via
 * {@link PluginRegistrationContext#registerStaticCondition(String, java.util.function.Predicate)},
 * so the common case ("apply only at the root", "apply only in an offensive slot") needs no
 * {@code Codec}, no {@code StaticCondition}, and no {@code ResolutionContext} import. Conditions
 * that carry JSON data still use the codec overload.
 *
 * <p>This deliberately exposes no {@code Component}: it answers structural questions
 * (root? depth? which slot?), not "give me the component", which would re-leak internals.
 */
public interface ConditionContext {

	/** @return true if the component being evaluated is the root of its assembly. */
	boolean isRoot();

	/** @return the component's depth in the tree (root is 0). */
	int depth();

	/** @return the type of the slot or structure part this component occupies, if any. */
	Optional<OpenIdentifier> slotType();

	/** @return the identity tags of the upgrade slot this component occupies (empty otherwise). */
	Set<OpenIdentifier> slotTags();

	/** @return true if this component sits in a slot whose type or tags match {@code type}. */
	default boolean isInSlotType(OpenIdentifier type) {
		return slotType().map(type::equals).orElse(false) || slotTags().contains(type);
	}

	/**
	 * Adapts the internal resolution context to this public view. Called by the framework, not by
	 * addon code.
	 */
	static ConditionContext of(ResolutionContext ctx) {
		return new ConditionContext() {
			@Override
			public boolean isRoot() {
				return ctx.isRoot();
			}

			@Override
			public int depth() {
				return ctx.getDepth();
			}

			@Override
			public Optional<OpenIdentifier> slotType() {
				return ctx.getSlot().map(Slot::slotType)
						.or(() -> ctx.getPart().map(part -> part.partType()));
			}

			@Override
			public Set<OpenIdentifier> slotTags() {
				return ctx.getSlot()
						.filter(slot -> slot instanceof ComponentUpgradeSlot)
						.map(slot -> ((ComponentUpgradeSlot) slot).tags())
						.orElse(Set.of());
			}
		};
	}
}
