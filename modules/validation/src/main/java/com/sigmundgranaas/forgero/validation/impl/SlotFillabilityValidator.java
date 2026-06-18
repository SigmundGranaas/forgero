package com.sigmundgranaas.forgero.validation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.StructuredComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.data.pipeline.api.ParsingError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates that every upgrade slot can actually be filled by <em>some</em> piece of content.
 * <p>
 * A slot's content is gated by its {@link com.sigmundgranaas.forgero.core.component.api.slot.SlotValidator},
 * which (for the common {@code required_tag}/slot-{@code type} case) is a <strong>literal</strong> tag
 * membership test — {@code component.getTags().contains(requiredTag)} — with no tag-graph walk. So a
 * slot whose required tag is carried by no content component is silently <em>unfillable</em>: the build
 * is green, the slot renders, and nothing can ever go in it. (This is exactly how {@code diamond-pickaxe}'s
 * {@code binding_slot}/{@code handle_grip_slot}/{@code tip_reinforcement_slot} — typed against tags no
 * content declares — became dead.)
 * <p>
 * This is the install-side complement to {@link SlotReferenceValidator}: that one catches an
 * {@code in_slot_type} <em>condition</em> referencing a slot nothing provides; this one catches a
 * <em>slot</em> requiring a tag no content carries.
 * <p>
 * <b>Why this mirrors runtime exactly:</b> the universe of installable content is the set of built
 * components, and the check {@code allContentTags.contains(requiredTag)} is the same predicate
 * {@code SlotValidator.test} applies. The validator is deliberately <em>conservative</em>: it only
 * reports a slot whose validator exposes a single {@code requiredType} (the {@code required_tag}/
 * {@code type} case). Slots with an accept-all validator, a multi-tag {@code valid_tags} conjunction,
 * or a custom predicate are skipped — never flagged — so there are no false positives.
 * <p>
 * <b>Severity:</b> findings are <em>errors</em> (build-failing). The original slot-type vocabulary debt
 * (slot {@code type}s authored against a {@code trinket}/{@code dye}/{@code binding}/… vocabulary the
 * content later moved off of) has been migrated to the tags content actually carries, so the corpus is
 * clean and any new unfillable slot is a regression that should fail the build. See
 * {@code docs/status/slot-vocabulary.md} for the migration and the per-family validator targets.
 */
public final class SlotFillabilityValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(SlotFillabilityValidator.class);

	private SlotFillabilityValidator() {
	}

	/**
	 * @param components       every built component (the universe of both slot owners and installable content)
	 * @param defaultNamespace namespace for the synthesized error identifier
	 * @return one error per distinct unfillable required tag, naming an example owning slot
	 */
	public static List<ParsingError> validate(Collection<Component> components, String defaultNamespace) {
		// The universe of installable content tags is every built component's declared tag set —
		// the same set SlotValidator.test() checks against.
		Set<OpenIdentifier> allContentTags = new HashSet<>();
		for (Component component : components) {
			allContentTags.addAll(component.getTags());
		}

		// Distinct required tag -> a sample slot that requires it (for a readable message).
		Map<OpenIdentifier, SlotRef> requiredTags = new LinkedHashMap<>();
		Set<Component> visited = newIdentitySet();
		for (Component component : components) {
			collect(component, requiredTags, visited);
		}

		List<ParsingError> errors = new ArrayList<>();
		for (Map.Entry<OpenIdentifier, SlotRef> entry : requiredTags.entrySet()) {
			OpenIdentifier requiredTag = entry.getKey();
			if (allContentTags.contains(requiredTag)) {
				continue;
			}
			SlotRef ref = entry.getValue();
			errors.add(new ParsingError(
					new OpenIdentifier(defaultNamespace, "slot_fillability"),
					"Unfillable slot '" + ref.slotId() + "' on '" + ref.owner() + "': its validator requires tag '"
							+ requiredTag + "', which no content component declares, so nothing can ever be installed "
							+ "in it. Type the slot against a tag real content carries (e.g. a binding slot should use "
							+ "forgero:materials/types/binding / forgero:parts/binding, matching its sibling tools).",
					null));
		}
		if (!errors.isEmpty()) {
			LOGGER.error("SLOT FILLABILITY ERRORS: {} slot type(s) require a tag no content provides (unfillable)", errors.size());
		}
		return errors;
	}

	private static void collect(Component current, Map<OpenIdentifier, SlotRef> requiredTags, Set<Component> visited) {
		if (!visited.add(current)) {
			return;
		}
		if (current instanceof CustomizableComponent customizable) {
			for (ComponentUpgradeSlot slot : customizable.getUpgradeSlots()) {
				OpenIdentifier required = slot.validator().requiredType();
				// Only the single-required-tag case is decidable here; accept-all / multi-tag / custom
				// validators expose a null requiredType and are deliberately not flagged.
				if (required != null) {
					requiredTags.putIfAbsent(required, new SlotRef(current.id(), slot.id()));
				}
			}
		}
		if (current instanceof StructuredComponent structured) {
			for (ComponentPart part : structured.structure().allParts()) {
				collect(part.content(), requiredTags, visited);
			}
		}
	}

	private static Set<Component> newIdentitySet() {
		return java.util.Collections.newSetFromMap(new IdentityHashMap<>());
	}

	private record SlotRef(OpenIdentifier owner, OpenIdentifier slotId) {
	}
}
