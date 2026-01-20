package com.sigmundgranaas.forgero.fabric.nbt.v2;

import static com.sigmundgranaas.forgero.core.type.Type.TOOL_PART_HEAD;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.TypeData;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.state.composite.Constructed;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedComposite;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedState;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.EmptySlot;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.FilledSlot;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.type.TypeTree;
import com.sigmundgranaas.forgero.fabric.resources.FabricPackFinder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompositeEncoder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompositeParser;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompoundEncoder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.SlotEncoder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.SlotParser;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.StateEncoder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.StateParser;
import com.sigmundgranaas.forgero.testutil.Materials;
import com.sigmundgranaas.forgero.testutil.Schematics;
import com.sigmundgranaas.forgero.testutil.ToolParts;
import com.sigmundgranaas.forgero.testutil.Tools;
import com.sigmundgranaas.forgero.testutil.Upgrades;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

/**
 * Comprehensive tests for NBT round-trip serialization of parts with upgrades.
 *
 * CRITICAL: These tests validate that when a part (like a pickaxe head) has an upgrade
 * installed (like a reinforcement), the upgrade data is properly:
 * 1. Encoded into NBT
 * 2. Parsed back from NBT
 * 3. Preserved through the full round-trip
 *
 * This test class was created to catch a critical bug where part-level upgrades
 * were being lost during serialization, while tool-level upgrades worked correctly.
 */
public class PartUpgradeNbtRoundTripTest {

	private static final CompoundEncoder<State> encoder = new CompositeEncoder();
	private static final CompositeParser parser = new CompositeParser(NbtToStateTest::ingredientSupplier);
	private static final StateEncoder stateEncoder = new StateEncoder();
	private static final SlotEncoder slotEncoder = new SlotEncoder(stateEncoder);
	private static final StateParser stateParser = new StateParser(NbtToStateTest::ingredientSupplier);
	private static final SlotParser slotParser = new SlotParser(stateParser);

	@BeforeAll
	public static void setup() {
		PipelineBuilder
				.builder()
				.register(FabricPackFinder.supplier())
				.state(ForgeroStateRegistry.stateListener())
				.state(ForgeroStateRegistry.compositeListener())
				.inflated(ForgeroStateRegistry.constructListener())
				.inflated(ForgeroStateRegistry.containerListener())
				.recipes(ForgeroStateRegistry.recipeListener())
				.build()
				.execute();
	}

	// ========== Slot Encoding Tests ==========

	@Nested
	@DisplayName("Slot Encoding Tests")
	class SlotEncodingTests {

		@Test
		@DisplayName("Empty slot encodes with correct codec type")
		void emptySlotEncodesWithCorrectCodecType() {
			EmptySlot emptySlot = new EmptySlot(0, Type.MATERIAL, "test_slot", Set.of(Category.OFFENSIVE));
			NbtCompound encoded = slotEncoder.encode(emptySlot);

			assertEquals(SlotEncoder.VALUE_EMPTY_SLOT_TYPE, encoded.getString(NbtConstants.KEY_CODEC_TYPE),
					"Empty slot must have correct codec type");
			assertEquals(0, encoded.getInt(NbtConstants.KEY_INDEX), "Index must be encoded");
			assertEquals("test_slot", encoded.getString(NbtConstants.KEY_DESCRIPTION), "Description must be encoded");
		}

		@Test
		@DisplayName("Filled slot encodes with upgrade content")
		void filledSlotEncodesWithUpgradeContent() {
			State upgradeContent = Materials.IRON;
			FilledSlot filledSlot = new FilledSlot(0, Type.MATERIAL, upgradeContent, "reinforcement", Set.of(Category.OFFENSIVE));

			NbtCompound encoded = slotEncoder.encode(filledSlot);

			assertEquals(SlotEncoder.VALUE_FILLED_SLOT_TYPE, encoded.getString(NbtConstants.KEY_CODEC_TYPE),
					"Filled slot must have correct codec type");
			assertTrue(encoded.contains(NbtConstants.KEY_UPGRADE), "Filled slot must contain upgrade data");

			NbtCompound upgradeNbt = encoded.getCompound(NbtConstants.KEY_UPGRADE);
			assertFalse(upgradeNbt.isEmpty(), "Upgrade NBT must not be empty");
		}

		@Test
		@DisplayName("Slot categories are encoded correctly")
		void slotCategoriesAreEncodedCorrectly() {
			EmptySlot slot = new EmptySlot(0, Type.MATERIAL, "test", Set.of(Category.OFFENSIVE, Category.UTILITY));
			NbtCompound encoded = slotEncoder.encode(slot);

			assertTrue(encoded.contains(NbtConstants.KEY_CATEGORIES), "Categories must be encoded");
			NbtList categories = encoded.getList(NbtConstants.KEY_CATEGORIES, NbtElement.STRING_TYPE);
			assertEquals(2, categories.size(), "Both categories must be encoded");
		}
	}

	// ========== Slot Parsing Tests ==========

	@Nested
	@DisplayName("Slot Parsing Tests")
	class SlotParsingTests {

		@Test
		@DisplayName("Empty slot round-trips correctly")
		void emptySlotRoundTripsCorrectly() {
			EmptySlot original = new EmptySlot(0, Type.MATERIAL, "test_slot", Set.of(Category.OFFENSIVE));

			NbtCompound encoded = slotEncoder.encode(original);
			Optional<Slot> parsed = slotParser.parse(encoded);

			assertTrue(parsed.isPresent(), "Slot must be parsed");
			Slot result = parsed.get();

			assertEquals(original.index(), result.index(), "Index must match");
			assertEquals(original.typeName(), result.typeName(), "Type must match");
			assertFalse(result.filled(), "Empty slot must remain empty after round-trip");
		}

		@Test
		@DisplayName("Filled slot round-trips with content preserved")
		void filledSlotRoundTripsWithContentPreserved() {
			// Use a REAL registered state from the registry instead of test fixture
			Optional<State> registeredIron = ForgeroStateRegistry.stateFinder().find("forgero:iron");

			// Debug: Check what identifiers exist
			System.out.println("=== DEBUG: Checking registry ===");
			System.out.println("Looking for 'forgero:iron': " + registeredIron.isPresent());
			System.out.println("STATES is null: " + (ForgeroStateRegistry.STATES == null));

			// Try to list some states that ARE in the registry
			if (ForgeroStateRegistry.STATES != null) {
				System.out.println("Listing first 20 registered state identifiers:");
				var allStates = ForgeroStateRegistry.STATES.all();
				int count = 0;
				for (var provider : allStates) {
					if (count < 20) {
						System.out.println("  " + provider.get().identifier());
						count++;
					}
				}
				System.out.println("Total states: " + allStates.size());
			}

			// If forgero:iron not found, try to find any registered material
			if (registeredIron.isEmpty()) {
				System.out.println("Trying other identifiers...");
				for (String testId : List.of("forgero:iron", "forgero:Iron", "iron", "Iron")) {
					var found = ForgeroStateRegistry.stateFinder().find(testId);
					System.out.println("  " + testId + ": " + found.isPresent());
				}
			}

			// Use test fixture as fallback but acknowledge it won't be found in registry
			State upgradeContent = registeredIron.orElse(Upgrades.IRON);
			System.out.println("Using upgrade content: " + upgradeContent.identifier());

			FilledSlot original = new FilledSlot(0, Type.MATERIAL, upgradeContent, "reinforcement", Set.of(Category.OFFENSIVE));

			NbtCompound encoded = slotEncoder.encode(original);

			// Debug: print the encoded NBT
			System.out.println("=== DEBUG: Encoded slot NBT ===");
			System.out.println("Full NBT: " + encoded);
			System.out.println("Codec type: " + encoded.getString(NbtConstants.KEY_CODEC_TYPE));
			if (encoded.contains(NbtConstants.KEY_UPGRADE)) {
				NbtCompound upgradeNbt = encoded.getCompound(NbtConstants.KEY_UPGRADE);
				System.out.println("Upgrade NBT: " + upgradeNbt);
				System.out.println("Upgrade ID: " + upgradeNbt.getString(NbtConstants.ID_IDENTIFIER));
				System.out.println("Upgrade STATE_TYPE: " + upgradeNbt.getString(NbtConstants.STATE_TYPE_IDENTIFIER));
			}

			// Debug: check if the state can be found in the registry
			String upgradeId = encoded.getCompound(NbtConstants.KEY_UPGRADE).getString(NbtConstants.ID_IDENTIFIER);
			Optional<State> foundInRegistry = ForgeroStateRegistry.stateFinder().find(upgradeId);
			System.out.println("State found in registry for '" + upgradeId + "': " + foundInRegistry.isPresent());

			Optional<Slot> parsed = slotParser.parse(encoded);

			System.out.println("Parsed slot present: " + parsed.isPresent());
			if (parsed.isPresent()) {
				System.out.println("Parsed slot filled: " + parsed.get().filled());
			}
			System.out.println("=== END DEBUG ===");

			assertTrue(parsed.isPresent(), "Filled slot must be parsed");
			Slot result = parsed.get();

			assertTrue(result.filled(), "CRITICAL: Filled slot must remain filled after round-trip!");
			assertTrue(result.get().isPresent(), "CRITICAL: Slot content must be present!");
			assertEquals(upgradeContent.identifier(), result.get().get().identifier(),
					"CRITICAL: Slot content identifier must match!");
		}
	}

	// ========== Part with Upgrade Tests ==========

	@Nested
	@DisplayName("Part with Upgrade Round-Trip Tests")
	class PartWithUpgradeTests {

		@Test
		@DisplayName("CRITICAL: Part with filled upgrade slot encodes and parses correctly")
		void partWithFilledUpgradeSlotRoundTrips() {
			// Create a part with an upgrade slot
			ArrayList<Slot> slots = new ArrayList<>();
			slots.add(new EmptySlot(0, Type.MATERIAL, "reinforcement", Set.of(Category.OFFENSIVE)));

			ConstructedState part = (ConstructedState) ConstructedComposite.ConstructBuilder.builder()
					.addIngredient(Materials.IRON)
					.addIngredient(Schematics.PICKAXE_HEAD_SCHEMATIC)
					.addSlotContainer(new SlotContainer(slots))
					.type(Type.PICKAXE_HEAD)
					.id("forgero:iron-pickaxe_head")
					.build();

			// Upgrade the part using the Upgradeable interface
			ConstructedState upgradedPart = (ConstructedState) part.upgrade(Materials.OAK);

			// Verify upgrade is installed
			assertTrue(upgradedPart.slots().stream().anyMatch(Slot::filled),
					"Part must have a filled slot before encoding");

			// Encode
			NbtCompound encoded = encoder.encode(upgradedPart);

			// Verify NBT contains upgrades
			assertTrue(encoded.contains(NbtConstants.UPGRADES_IDENTIFIER),
					"Encoded NBT must contain upgrades section");
			NbtList upgradesNbt = encoded.getList(NbtConstants.UPGRADES_IDENTIFIER, NbtElement.COMPOUND_TYPE);
			assertFalse(upgradesNbt.isEmpty(), "Upgrades list must not be empty");

			// Verify at least one slot is marked as filled
			boolean hasFilledSlot = upgradesNbt.stream()
					.filter(NbtCompound.class::isInstance)
					.map(NbtCompound.class::cast)
					.anyMatch(nbt -> SlotEncoder.VALUE_FILLED_SLOT_TYPE.equals(nbt.getString(NbtConstants.KEY_CODEC_TYPE)));
			assertTrue(hasFilledSlot, "CRITICAL: NBT must contain at least one filled slot!");

			// Parse back
			Optional<State> parsedOpt = parser.parse(encoded);
			assertTrue(parsedOpt.isPresent(), "Part must be parsed");

			ConstructedState parsed = (ConstructedState) parsedOpt.get();

			// CRITICAL CHECK: Verify upgrade survived
			boolean hasFilledSlotAfter = parsed.slots().stream().anyMatch(Slot::filled);
			assertTrue(hasFilledSlotAfter,
					"CRITICAL: Part must still have filled slot after round-trip! " +
					"Upgrade was lost during serialization.");
		}

		@Test
		@DisplayName("CRITICAL: Multiple slots with mixed fill states round-trip correctly")
		void multipleSlotsWithMixedFillStatesRoundTrip() {
			// Create slots - one filled, one empty
			FilledSlot filledSlot = new FilledSlot(0, Type.MATERIAL, Materials.IRON, "reinforcement", Set.of(Category.OFFENSIVE));
			EmptySlot emptySlot = new EmptySlot(1, Type.GEM, "gem_socket", Set.of(Category.UTILITY));

			ArrayList<Slot> slots = new ArrayList<>();
			slots.add(filledSlot);
			slots.add(emptySlot);

			ConstructedState part = (ConstructedState) ConstructedComposite.ConstructBuilder.builder()
					.addIngredient(Materials.IRON)
					.addIngredient(Schematics.PICKAXE_HEAD_SCHEMATIC)
					.addSlotContainer(new SlotContainer(slots))
					.type(Type.PICKAXE_HEAD)
					.id("forgero:test-pickaxe_head")
					.build();

			// Count slots before
			long filledBefore = part.slots().stream().filter(Slot::filled).count();
			long emptyBefore = part.slots().stream().filter(s -> !s.filled()).count();

			// Round-trip
			NbtCompound encoded = encoder.encode(part);
			Optional<State> parsedOpt = parser.parse(encoded);
			assertTrue(parsedOpt.isPresent(), "Part must be parsed");

			ConstructedState parsed = (ConstructedState) parsedOpt.get();

			// Count slots after
			long filledAfter = parsed.slots().stream().filter(Slot::filled).count();
			long emptyAfter = parsed.slots().stream().filter(s -> !s.filled()).count();

			assertEquals(filledBefore, filledAfter,
					"CRITICAL: Filled slot count must be preserved! Before: " + filledBefore + ", After: " + filledAfter);
			assertEquals(emptyBefore, emptyAfter,
					"Empty slot count must be preserved! Before: " + emptyBefore + ", After: " + emptyAfter);
		}
	}

	// ========== Tool with Upgraded Part Tests ==========

	@Nested
	@DisplayName("Tool with Upgraded Part Tests")
	class ToolWithUpgradedPartTests {

		@Test
		@DisplayName("CRITICAL: Tool with upgraded part encodes part's slots")
		void toolWithUpgradedPartEncodesPartSlots() {
			TypeTree tree = new TypeTree();
			tree.addNode(new TypeData(TOOL_PART_HEAD.typeName(), Optional.empty(), Collections.emptyList()));
			tree.addNode(new TypeData(Type.PICKAXE_HEAD.typeName(), Optional.of(TOOL_PART_HEAD.typeName()), Collections.emptyList()));
			ForgeroStateRegistry.TREE = tree;

			try {
				// Create a part with an upgrade
				ArrayList<Slot> partSlots = new ArrayList<>();
				partSlots.add(new EmptySlot(0, Type.MATERIAL, "reinforcement", Set.of(Category.OFFENSIVE)));

				ConstructedState head = (ConstructedState) ConstructedComposite.ConstructBuilder.builder()
						.addIngredient(Materials.IRON)
						.addIngredient(Schematics.PICKAXE_HEAD_SCHEMATIC)
						.addSlotContainer(new SlotContainer(partSlots))
						.type(Type.PICKAXE_HEAD)
						.id("forgero:iron-pickaxe_head")
						.build();

				// Upgrade the head
				ConstructedState upgradedHead = (ConstructedState) head.upgrade(Upgrades.IRON);

				// Verify head has filled slot
				assertTrue(upgradedHead.slots().stream().anyMatch(Slot::filled),
						"Head must have filled slot before building tool");

				// Build tool with upgraded head
				State handle = ToolParts.HANDLE;
				ArrayList<Slot> toolSlots = new ArrayList<>();
				toolSlots.add(new EmptySlot(0, Type.BINDING, "", Set.of(Category.UTILITY)));

				State tool = ConstructedComposite.ConstructBuilder.builder()
						.addIngredient(upgradedHead)
						.addIngredient(handle)
						.addSlotContainer(new SlotContainer(toolSlots))
						.type(Type.PICKAXE)
						.id("forgero:iron-pickaxe")
						.build();

				// Encode the tool
				NbtCompound encoded = encoder.encode(tool);

				// Verify ingredients are encoded
				assertTrue(encoded.contains(NbtConstants.INGREDIENTS_IDENTIFIER),
						"Tool must have ingredients encoded");
				NbtList ingredients = encoded.getList(NbtConstants.INGREDIENTS_IDENTIFIER, NbtElement.COMPOUND_TYPE);

				// Find the head in ingredients
				boolean foundHeadWithUpgrade = false;
				for (int i = 0; i < ingredients.size(); i++) {
					NbtCompound ingredientNbt = ingredients.getCompound(i);
					if (ingredientNbt.contains(NbtConstants.UPGRADES_IDENTIFIER)) {
						NbtList upgradesNbt = ingredientNbt.getList(NbtConstants.UPGRADES_IDENTIFIER, NbtElement.COMPOUND_TYPE);
						for (int j = 0; j < upgradesNbt.size(); j++) {
							NbtCompound slotNbt = upgradesNbt.getCompound(j);
							if (SlotEncoder.VALUE_FILLED_SLOT_TYPE.equals(slotNbt.getString(NbtConstants.KEY_CODEC_TYPE))) {
								foundHeadWithUpgrade = true;
								break;
							}
						}
					}
				}

				assertTrue(foundHeadWithUpgrade,
						"CRITICAL: Tool's ingredient (head) must have its filled upgrade slot encoded!");

			} finally {
				ForgeroStateRegistry.TREE = null;
			}
		}

		@Test
		@DisplayName("CRITICAL: Tool with upgraded part round-trips preserving part upgrades")
		void toolWithUpgradedPartRoundTripsPreservingPartUpgrades() {
			TypeTree tree = new TypeTree();
			tree.addNode(new TypeData(TOOL_PART_HEAD.typeName(), Optional.empty(), Collections.emptyList()));
			tree.addNode(new TypeData(Type.PICKAXE_HEAD.typeName(), Optional.of(TOOL_PART_HEAD.typeName()), Collections.emptyList()));
			ForgeroStateRegistry.TREE = tree;

			try {
				// Create upgraded head
				FilledSlot reinforcement = new FilledSlot(0, Type.MATERIAL, Upgrades.IRON, "reinforcement", Set.of(Category.OFFENSIVE));
				ArrayList<Slot> partSlots = new ArrayList<>();
				partSlots.add(reinforcement);

				State upgradedHead = ConstructedComposite.ConstructBuilder.builder()
						.addIngredient(Materials.IRON)
						.addIngredient(Schematics.PICKAXE_HEAD_SCHEMATIC)
						.addSlotContainer(new SlotContainer(partSlots))
						.type(Type.PICKAXE_HEAD)
						.id("forgero:iron-pickaxe_head")
						.build();

				// Build tool
				State handle = ToolParts.HANDLE;
				ArrayList<Slot> toolSlots = new ArrayList<>();
				toolSlots.add(new EmptySlot(0, Type.BINDING, "", Set.of(Category.UTILITY)));

				State tool = ConstructedComposite.ConstructBuilder.builder()
						.addIngredient(upgradedHead)
						.addIngredient(handle)
						.addSlotContainer(new SlotContainer(toolSlots))
						.type(Type.PICKAXE)
						.id("forgero:iron-pickaxe")
						.build();

				// Count filled slots in parts before
				long filledInPartsBefore = 0;
				if (tool instanceof ConstructedState constructedTool) {
					for (State part : constructedTool.parts()) {
						if (part instanceof ConstructedState constructedPart) {
							filledInPartsBefore += constructedPart.slots().stream().filter(Slot::filled).count();
						}
					}
				}
				assertTrue(filledInPartsBefore > 0, "Tool's parts must have filled slots before round-trip");

				// Round-trip
				NbtCompound encoded = encoder.encode(tool);
				Optional<State> parsedOpt = parser.parse(encoded);
				assertTrue(parsedOpt.isPresent(), "Tool must be parsed");

				State parsed = parsedOpt.get();

				// Count filled slots in parts after
				long filledInPartsAfter = 0;
				if (parsed instanceof ConstructedState parsedTool) {
					for (State part : parsedTool.parts()) {
						if (part instanceof ConstructedState parsedPart) {
							filledInPartsAfter += parsedPart.slots().stream().filter(Slot::filled).count();
						}
					}
				}

				assertEquals(filledInPartsBefore, filledInPartsAfter,
						"CRITICAL: Part upgrades must be preserved in tool after round-trip! " +
						"Before: " + filledInPartsBefore + ", After: " + filledInPartsAfter);

			} finally {
				ForgeroStateRegistry.TREE = null;
			}
		}
	}

	// ========== Comparison with Working Tool-Level Upgrades ==========

	@Nested
	@DisplayName("Comparison: Tool-Level vs Part-Level Upgrades")
	class ComparisonTests {

		@Test
		@DisplayName("Tool-level upgrade (binding) round-trips correctly (baseline)")
		void toolLevelUpgradeRoundTripsCorrectly() {
			// This is the baseline test that should pass - tool-level upgrades work
			ConstructedState toolWithBinding = (ConstructedState) Tools.IRON_PICKAXE.get().upgrade(Upgrades.BINDING);

			// Verify binding is installed
			assertTrue(toolWithBinding.slots().stream().anyMatch(Slot::filled),
					"Tool must have binding before encoding");

			// Round-trip
			NbtCompound encoded = encoder.encode(toolWithBinding);
			Optional<State> parsedOpt = parser.parse(encoded);
			assertTrue(parsedOpt.isPresent(), "Tool must be parsed");

			ConstructedState parsed = (ConstructedState) parsedOpt.get();

			// Verify binding survived (this should pass)
			boolean hasFilledSlot = parsed.slots().stream().anyMatch(Slot::filled);
			assertTrue(hasFilledSlot,
					"Tool-level upgrade must survive round-trip (baseline test)");
		}

		@Test
		@DisplayName("CRITICAL: Part-level upgrade should round-trip just like tool-level")
		void partLevelUpgradeShouldRoundTripLikeToolLevel() {
			// Create a simple part with upgrade (similar structure to tool with binding)
			ArrayList<Slot> slots = new ArrayList<>();
			slots.add(new EmptySlot(0, Type.MATERIAL, "reinforcement", Set.of(Category.OFFENSIVE)));

			ConstructedState part = (ConstructedState) ConstructedComposite.ConstructBuilder.builder()
					.addIngredient(Materials.IRON)
					.addIngredient(Schematics.PICKAXE_HEAD_SCHEMATIC)
					.addSlotContainer(new SlotContainer(slots))
					.type(Type.PICKAXE_HEAD)
					.id("forgero:test-part")
					.build();

			// Upgrade the part
			ConstructedState upgradedPart = (ConstructedState) part.upgrade(Upgrades.IRON);

			// Verify upgrade is installed
			long filledBefore = upgradedPart.slots().stream().filter(Slot::filled).count();
			assertTrue(filledBefore > 0, "Part must have filled slot before encoding");

			// Round-trip
			NbtCompound encoded = encoder.encode(upgradedPart);
			Optional<State> parsedOpt = parser.parse(encoded);
			assertTrue(parsedOpt.isPresent(), "Part must be parsed");

			ConstructedState parsed = (ConstructedState) parsedOpt.get();

			// CRITICAL: This is the test that may fail if there's a bug
			long filledAfter = parsed.slots().stream().filter(Slot::filled).count();
			assertEquals(filledBefore, filledAfter,
					"CRITICAL: Part-level upgrade must survive round-trip just like tool-level upgrades! " +
					"If this fails but tool-level works, there's a bug in part serialization. " +
					"Before: " + filledBefore + ", After: " + filledAfter);
		}
	}

	// ========== Edge Cases ==========

	@Nested
	@DisplayName("Edge Cases")
	class EdgeCases {

		@Test
		@DisplayName("Part with no slots round-trips correctly")
		void partWithNoSlotsRoundTrips() {
			State part = ConstructedComposite.ConstructBuilder.builder()
					.addIngredient(Materials.IRON)
					.addIngredient(Schematics.PICKAXE_HEAD_SCHEMATIC)
					.type(Type.PICKAXE_HEAD)
					.id("forgero:simple-part")
					.build();

			NbtCompound encoded = encoder.encode(part);
			Optional<State> parsedOpt = parser.parse(encoded);

			assertTrue(parsedOpt.isPresent(), "Part with no slots must still parse");
		}

		@Test
		@DisplayName("Part with all slots empty round-trips correctly")
		void partWithAllSlotsEmptyRoundTrips() {
			ArrayList<Slot> slots = new ArrayList<>();
			slots.add(new EmptySlot(0, Type.MATERIAL, "slot1", Set.of(Category.OFFENSIVE)));
			slots.add(new EmptySlot(1, Type.GEM, "slot2", Set.of(Category.UTILITY)));

			ConstructedState part = (ConstructedState) ConstructedComposite.ConstructBuilder.builder()
					.addIngredient(Materials.IRON)
					.addIngredient(Schematics.PICKAXE_HEAD_SCHEMATIC)
					.addSlotContainer(new SlotContainer(slots))
					.type(Type.PICKAXE_HEAD)
					.id("forgero:empty-slots-part")
					.build();

			int slotCountBefore = part.slots().size();

			NbtCompound encoded = encoder.encode(part);
			Optional<State> parsedOpt = parser.parse(encoded);
			assertTrue(parsedOpt.isPresent(), "Part must parse");

			ConstructedState parsed = (ConstructedState) parsedOpt.get();
			int slotCountAfter = parsed.slots().size();

			assertEquals(slotCountBefore, slotCountAfter,
					"Empty slot count must be preserved");
		}

		@Test
		@DisplayName("Part with all slots filled round-trips correctly")
		void partWithAllSlotsFilledRoundTrips() {
			ArrayList<Slot> slots = new ArrayList<>();
			slots.add(new FilledSlot(0, Type.MATERIAL, Upgrades.IRON, "slot1", Set.of(Category.OFFENSIVE)));
			slots.add(new FilledSlot(1, Type.MATERIAL, Materials.OAK, "slot2", Set.of(Category.UTILITY)));

			ConstructedState part = (ConstructedState) ConstructedComposite.ConstructBuilder.builder()
					.addIngredient(Materials.IRON)
					.addIngredient(Schematics.PICKAXE_HEAD_SCHEMATIC)
					.addSlotContainer(new SlotContainer(slots))
					.type(Type.PICKAXE_HEAD)
					.id("forgero:full-slots-part")
					.build();

			long filledBefore = part.slots().stream().filter(Slot::filled).count();
			assertEquals(2, filledBefore, "Both slots must be filled before");

			NbtCompound encoded = encoder.encode(part);
			Optional<State> parsedOpt = parser.parse(encoded);
			assertTrue(parsedOpt.isPresent(), "Part must parse");

			ConstructedState parsed = (ConstructedState) parsedOpt.get();
			long filledAfter = parsed.slots().stream().filter(Slot::filled).count();

			assertEquals(filledBefore, filledAfter,
					"CRITICAL: All filled slots must be preserved! Before: " + filledBefore + ", After: " + filledAfter);
		}
	}
}
