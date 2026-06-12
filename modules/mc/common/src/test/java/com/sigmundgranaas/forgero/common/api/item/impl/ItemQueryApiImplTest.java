package com.sigmundgranaas.forgero.common.api.item.impl;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.slot.InstallationResult;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotQuery;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ItemQueryApiImpl API contract.
 * <p>
 * These tests verify the documented behavior of the ItemQueryApi:
 * - All methods must handle null ItemStack gracefully (return sensible defaults)
 * - Boolean methods return false for null/non-Forgero items
 * - Numeric methods return 0 for null/non-Forgero items
 * - Collection methods return empty collections for null/non-Forgero items
 * <p>
 * Full integration tests with real Forgero items require GameTest.
 */
@DisplayName("ItemQueryApiImpl API Contract")
class ItemQueryApiImplTest {

	private ItemQueryApiImpl api;

	@BeforeEach
	void setUp() {
		api = new ItemQueryApiImpl(new StubComponentConverter(), new StubSlotManager(),
				new com.sigmundgranaas.forgero.common.tags.engine.TaggedRegistry.Builder<com.sigmundgranaas.forgero.core.component.api.Component>(
						com.sigmundgranaas.forgero.common.tags.engine.EmptyTagResolver.INSTANCE).build());
	}

	@Nested
	@DisplayName("API Contract: Null Safety")
	class NullSafetyContract {

		@Test
		@DisplayName("All boolean query methods return false for null input")
		void booleanMethodsReturnFalseForNull() {
			assertAll("Boolean methods should return false for null",
				() -> assertFalse(api.isForgeroItem(null), "isForgeroItem"),
				() -> assertFalse(api.isCustomizable(null), "isCustomizable"),
				() -> assertFalse(api.hasEmptySlots(null), "hasEmptySlots"),
				() -> assertFalse(api.isFullyUpgraded(null), "isFullyUpgraded"),
				() -> assertFalse(api.appliesAttributes(null), "appliesAttributes"),
				() -> assertFalse(api.hasTag(null, OpenIdentifier.parse("forgero:test")), "hasTag")
			);
		}

		@Test
		@DisplayName("All numeric query methods return 0 for null input")
		void numericMethodsReturnZeroForNull() {
			assertAll("Numeric methods should return 0 for null",
				() -> assertEquals(0, api.getPartCount(null), "getPartCount"),
				() -> assertEquals(0, api.getDurability(null), "getDurability"),
				() -> assertEquals(0, api.getMaxDurability(null), "getMaxDurability"),
				() -> assertEquals(0, api.getMiningLevel(null), "getMiningLevel"),
				() -> assertEquals(0, api.getArmor(null), "getArmor"),
				() -> assertEquals(0, api.getUpgradeSlotCount(null), "getUpgradeSlotCount"),
				() -> assertEquals(0, api.getFilledSlotCount(null), "getFilledSlotCount"),
				() -> assertEquals(0, api.getEmptySlotCount(null), "getEmptySlotCount"),
				() -> assertEquals(0, api.getContributedDurability(null), "getContributedDurability")
			);
		}

		@Test
		@DisplayName("All float query methods return 0.0f for null input")
		void floatMethodsReturnZeroForNull() {
			assertAll("Float methods should return 0.0f for null",
				() -> assertEquals(0.0f, api.getAttackDamage(null), "getAttackDamage"),
				() -> assertEquals(0.0f, api.getMiningSpeed(null), "getMiningSpeed"),
				() -> assertEquals(0.0f, api.getAttackSpeed(null), "getAttackSpeed"),
				() -> assertEquals(0.0f, api.getArmorToughness(null), "getArmorToughness"),
				() -> assertEquals(0.0f, api.getAttribute(null, DefaultAttributes.ATTACK_DAMAGE), "getAttribute"),
				() -> assertEquals(0.0f, api.getContributedAttackDamage(null), "getContributedAttackDamage"),
				() -> assertEquals(0.0f, api.getContributedMiningSpeed(null), "getContributedMiningSpeed")
			);
		}

		@Test
		@DisplayName("All collection query methods return empty collections for null input")
		void collectionMethodsReturnEmptyForNull() {
			assertAll("Collection methods should return empty for null",
				() -> assertTrue(api.getComponents(null).isEmpty(), "getComponents"),
				() -> assertTrue(api.getParts(null).isEmpty(), "getParts"),
				() -> assertTrue(api.getInstalledUpgrades(null).isEmpty(), "getInstalledUpgrades"),
				() -> assertTrue(api.getTags(null).isEmpty(), "getTags")
			);
		}

		@Test
		@DisplayName("Optional query methods return empty for null input")
		void optionalMethodsReturnEmptyForNull() {
			assertTrue(api.getPrimaryMaterial(null).isEmpty(), "getPrimaryMaterial should return empty for null");
		}

		@Test
		@DisplayName("getAttribute returns 0.0f when attribute type is null")
		void getAttributeReturnsZeroForNullAttributeType() {
			assertEquals(0.0f, api.getAttribute(null, null), "Should return 0 when both params are null");
		}

		@Test
		@DisplayName("hasTag returns false when tag is null")
		void hasTagReturnsFalseForNullTag() {
			assertFalse(api.hasTag(null, null), "Should return false when both params are null");
		}
	}

	// ========== Stub implementations for testing ==========
	// These return empty/default values since we can't create real ItemStacks without Minecraft

	private static class StubComponentConverter implements ComponentConverter {
		@Override public Optional<Component> toComponent(ItemStack stack) { return Optional.empty(); }
		@Override public Optional<Component> toComponent(Item item) { return Optional.empty(); }
		@Override public Optional<Component> toComponent(Identifier itemId) { return Optional.empty(); }
		@Override public Optional<ItemStack> toStack(Component component) { return Optional.empty(); }
		@Override public Optional<ItemStack> toStack(OpenIdentifier componentId) { return Optional.empty(); }
		@Override public Optional<Item> toItem(Component component) { return Optional.empty(); }
		@Override public Optional<Item> toItem(OpenIdentifier componentId) { return Optional.empty(); }
		@Override public Optional<Identifier> toItemId(OpenIdentifier componentId) { return Optional.empty(); }
		@Override public Optional<OpenIdentifier> toComponentId(ItemStack stack) { return Optional.empty(); }
		@Override public Optional<OpenIdentifier> toComponentId(Item item) { return Optional.empty(); }
	}

	private static class StubSlotManager implements SlotManager {
		@Override public boolean hasComponentUpgradeSlots(Component c) { return false; }
		@Override public int countComponentUpgradeSlots(Component c) { return 0; }
		@Override public int countFilledSlots(Component c) { return 0; }
		@Override public int countEmptySlots(Component c) { return 0; }
		@Override public List<ComponentUpgradeSlot> getFilledUpgradeSlots(Component c) { return List.of(); }
		@Override public SlotQuery<ComponentUpgradeSlot> queryUpgradeSlots(Component c) { throw new UnsupportedOperationException(); }
		@Override public List<ComponentPart> getStructureParts(Component c) { return List.of(); }
		@Override public List<ComponentUpgradeSlot> getAllUpgradeSlots(Component c) { return List.of(); }
		@Override public List<ComponentUpgradeSlot> getEmptyUpgradeSlots(Component c) { return List.of(); }
		@Override public List<Component> getInstalledUpgrades(Component c) { return List.of(); }
		@Override public boolean canInstall(Component t, Component u) { return false; }
		@Override public Optional<ComponentUpgradeSlot> findCompatibleSlot(Component t, Component u) { return Optional.empty(); }
		@Override public List<ComponentUpgradeSlot> findAllCompatibleSlots(Component t, Component u) { return List.of(); }
		@Override public boolean isCompatible(ComponentUpgradeSlot s, Component u) { return false; }
		@Override public Optional<String> validateUpgrade(ComponentUpgradeSlot s, Component u) { return Optional.empty(); }
		@Override public InstallationResult install(Component t, Component u) { return InstallationResult.failure("stub"); }
		@Override public Component installInSlot(Component t, OpenIdentifier s, Component u) { throw new UnsupportedOperationException(); }
		@Override public Component installOrReplace(Component t, OpenIdentifier s, Component u) { throw new UnsupportedOperationException(); }
		@Override public Component removeUpgrade(Component t, OpenIdentifier u) { throw new UnsupportedOperationException(); }
		@Override public Component removeFromSlot(Component t, OpenIdentifier s) { throw new UnsupportedOperationException(); }
		@Override public Component removeAllUpgrades(Component t) { throw new UnsupportedOperationException(); }
		@Override public boolean areAllSlotsFilled(Component c) { return false; }
	}
}
