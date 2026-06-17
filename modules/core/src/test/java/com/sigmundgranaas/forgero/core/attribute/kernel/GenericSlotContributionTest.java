package com.sigmundgranaas.forgero.core.attribute.kernel;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.BakedAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.component.impl.ExtensiblePart;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Proves the stat kernel folds <em>any</em> {@link Slot} kind that contributes a Component, not
 * just {@code ComponentUpgradeSlot} — the Phase-1 genericity. Participation is driven by the
 * generic {@link Slot#includeInTraversal()} and {@link Slot#componentContent()} hooks, so a
 * plugin-defined slot kind (a rune, a potion modeled as a Component, …) folds into the parent with
 * no kernel changes. Before this change, {@code StatFold} short-circuited on
 * {@code instanceof ComponentUpgradeSlot} and ignored every other kind.
 */
class GenericSlotContributionTest {

	private static final OpenIdentifier DUR = OpenIdentifier.parse("forgero:durability");

	private static OpenIdentifier id(String s) {
		return OpenIdentifier.parse("forgero:" + s);
	}

	private static Component durabilityLeaf(String name, float value) {
		return new StaticComponent(id(name), Set.of(id(name + "_tag")),
				Map.of(Attribute.KEY.key(), List.of(new SimpleAttribute(DUR, value))));
	}

	private static Component partWithSlot(float baseDurability, Slot slot) {
		ComponentUpgrades upgrades = new ComponentUpgrades(SlotContainer.of(slot));
		return new ExtensiblePart(id("blade"), Set.of(id("tool")),
				Map.of(Attribute.KEY.key(), List.of(new SimpleAttribute(DUR, baseDurability))), upgrades);
	}

	/** A non-upgrade slot kind that holds a Component (e.g. a rune / potion-as-Component). */
	private record ComponentHoldingSlot(OpenIdentifier id, Optional<Component> content, boolean traverse)
			implements Slot {
		@Override
		public OpenIdentifier type() {
			return OpenIdentifier.parse("test:component_slot");
		}

		@Override
		public OpenIdentifier slotType() {
			return OpenIdentifier.parse("test:custom");
		}

		@Override
		public String description() {
			return "test component-holding slot";
		}

		@Override
		public boolean includeInTraversal() {
			return traverse;
		}

		@Override
		public Optional<Component> componentContent() {
			return content;
		}
	}

	/** A slot kind that holds non-Component state (defaults: no componentContent). */
	private record DataSlot(OpenIdentifier id) implements Slot {
		@Override
		public OpenIdentifier type() {
			return OpenIdentifier.parse("test:data_slot");
		}

		@Override
		public OpenIdentifier slotType() {
			return OpenIdentifier.parse("test:data");
		}

		@Override
		public String description() {
			return "test data slot";
		}
	}

	@Test
	void componentHoldingSlotKind_foldsThroughGenericHook() {
		Slot rune = new ComponentHoldingSlot(id("rune_slot"), Optional.of(durabilityLeaf("rune", 40f)), true);
		BakedAttributes baked = StatFold.fold(partWithSlot(100f, rune));
		assertEquals(140f, baked.get(DUR).value(), 0.001f,
				"a non-ComponentUpgradeSlot kind holding a Component must fold into the parent: 100 + 40");
	}

	@Test
	void slotOptingOutOfTraversal_contributesNothing() {
		Slot rune = new ComponentHoldingSlot(id("rune_slot"), Optional.of(durabilityLeaf("rune", 40f)), false);
		BakedAttributes baked = StatFold.fold(partWithSlot(100f, rune));
		assertEquals(100f, baked.get(DUR).value(), 0.001f,
				"includeInTraversal()=false ⇒ the slot's content is not folded");
	}

	@Test
	void nonComponentSlotKind_isSkippedHarmlessly() {
		Slot data = new DataSlot(id("data_slot"));
		BakedAttributes baked = StatFold.fold(partWithSlot(100f, data));
		assertEquals(100f, baked.get(DUR).value(), 0.001f,
				"a slot holding non-Component state contributes nothing and does not error");
	}
}
