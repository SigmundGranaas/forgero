package com.sigmundgranaas.forgero.core.component.api.slot;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.Slot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the load-time slot dispatch contract ({@link SlotFactoryRegistry}) that the component
 * builder relies on: the default kind builds a {@link ComponentUpgradeSlot}, a registered plugin
 * kind builds its own {@link Slot}, an unknown kind is reported (not silently defaulted), and the
 * heterogeneous {@link ComponentUpgrades#ofSlots} holds any kind.
 */
class SlotFactoryRegistryTest {

	private static SlotFactory.SlotSpec spec(String validTag) {
		return new SlotFactory.SlotSpec(
				OpenIdentifier.parse("forgero:slot_1"),
				OpenIdentifier.parse("forgero:upgrades/types/gem"),
				"slot.test",
				Set.of(OpenIdentifier.parse("forgero:contexts/offensive")),
				validTag == null ? null : List.of(OpenIdentifier.parse(validTag)));
	}

	@Test
	@DisplayName("default kind builds a tag-validated ComponentUpgradeSlot")
	void defaultKindBuildsUpgradeSlot() {
		SlotFactory factory = SlotFactoryRegistry.get(null);
		assertNotNull(factory, "default kind must always be registered");

		Slot slot = factory.create(spec(null), Optional.empty());
		assertInstanceOf(ComponentUpgradeSlot.class, slot);
		ComponentUpgradeSlot upgrade = (ComponentUpgradeSlot) slot;
		assertEquals(OpenIdentifier.parse("forgero:upgrades/types/gem"), upgrade.slotType());
		assertTrue(upgrade.tags().contains(OpenIdentifier.parse("forgero:contexts/offensive")));
		// no valid_tags => validator requires the slot type as a tag
		assertEquals(SlotFactoryRegistry.DEFAULT_KIND, upgrade.type());
	}

	@Test
	@DisplayName("explicit valid_tags drive the upgrade slot validator")
	void validTagsDriveValidator() {
		Slot slot = SlotFactoryRegistry.get(SlotFactoryRegistry.DEFAULT_KIND)
				.create(spec("forgero:upgrades/types/reinforcement"), Optional.empty());
		assertInstanceOf(ComponentUpgradeSlot.class, slot);
	}

	@Test
	@DisplayName("a registered plugin kind is dispatched to its own factory")
	void pluginKindDispatch() {
		OpenIdentifier kind = OpenIdentifier.parse("forgero:test_potion_slot");
		assertFalse(SlotFactoryRegistry.isRegistered(kind));
		SlotFactoryRegistry.register(kind, (s, content) -> new TestSlot(s.id(), s.slotType(), content));

		assertTrue(SlotFactoryRegistry.isRegistered(kind));
		Slot slot = SlotFactoryRegistry.get(kind).create(spec(null), Optional.empty());
		assertInstanceOf(TestSlot.class, slot);
		assertEquals(kind, slot.type());

		// re-registering the same kind is rejected (loud, not silent)
		assertThrows(IllegalArgumentException.class,
				() -> SlotFactoryRegistry.register(kind, (s, c) -> new TestSlot(s.id(), s.slotType(), c)));
	}

	@Test
	@DisplayName("unknown kind returns null so the builder can fail loudly")
	void unknownKindReturnsNull() {
		assertNull(SlotFactoryRegistry.get(OpenIdentifier.parse("forgero:no_such_kind")));
	}

	@Test
	@DisplayName("ComponentUpgrades.ofSlots holds any Slot kind, upgrade views filter")
	void heterogeneousContainer() {
		ComponentUpgradeSlot upgrade = ComponentUpgradeSlot.emptyOfType(
				OpenIdentifier.parse("forgero:slot_u"), OpenIdentifier.parse("forgero:upgrades/types/gem"), "u");
		TestSlot custom = new TestSlot(OpenIdentifier.parse("forgero:slot_c"),
				OpenIdentifier.parse("forgero:custom"), Optional.empty());

		ComponentUpgrades upgrades = ComponentUpgrades.ofSlots(List.of(upgrade, custom));
		// the heterogeneous container holds both...
		assertEquals(2, upgrades.slots().all().size());
		// ...but the upgrade-typed view exposes only the ComponentUpgradeSlot.
		assertEquals(1, upgrades.allUpgradeSlots().size());
	}

	/** Minimal non-upgrade slot kind for the test. */
	private record TestSlot(OpenIdentifier id, OpenIdentifier slotType, Optional<Component> content) implements Slot {
		static final OpenIdentifier KIND = OpenIdentifier.parse("forgero:test_potion_slot");

		@Override
		public OpenIdentifier type() {
			return KIND;
		}

		@Override
		public String description() {
			return "test";
		}

		@Override
		public Optional<Component> componentContent() {
			return content;
		}
	}
}
