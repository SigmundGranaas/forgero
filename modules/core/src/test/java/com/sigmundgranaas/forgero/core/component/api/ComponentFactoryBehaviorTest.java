package com.sigmundgranaas.forgero.core.component.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgrades;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ComponentFactory Behavior")
class ComponentFactoryBehaviorTest {

	private static final OpenIdentifier IRON_ID = OpenIdentifier.of("iron");
	private static final OpenIdentifier METAL_TAG = OpenIdentifier.of("metal");
	private static final OpenIdentifier PICKAXE_ID = OpenIdentifier.of("pickaxe");
	private static final OpenIdentifier GEM_TYPE = OpenIdentifier.of("gem");

	@Nested
	@DisplayName("Part Creation")
	class PartCreation {

		@Test
		@DisplayName("creates StaticComponent when no structure or upgrades")
		void createsStaticComponentWhenNoStructureOrUpgrades() {
			Component component = ComponentFactory.create(
					IRON_ID,
					Set.of(METAL_TAG),
					Map.of(),
					null,
					null,
					false
			);

			assertFalse(component instanceof StructuredComponent);
			assertFalse(component instanceof CustomizableComponent);
			assertEquals("forgero:static_component", component.getTypeIdentifier().toString());
		}

		@Test
		@DisplayName("creates StructuredPart when structure provided")
		void createsStructuredPartWhenStructureProvided() {
			Component material = ComponentFactory.createStatic(IRON_ID, Set.of(OpenIdentifier.of("material")), Map.of());
			ComponentStructure structure = ComponentStructure.of(List.of(
					ComponentPart.ofType(OpenIdentifier.of("material"), OpenIdentifier.of("material"), "", material)
			));

			Component part = ComponentFactory.create(
					PICKAXE_ID,
					Set.of(),
					Map.of(),
					structure,
					null,
					false
			);

			assertInstanceOf(StructuredComponent.class, part);
			assertFalse(part instanceof CustomizableComponent);
			assertEquals("forgero:structured_part", part.getTypeIdentifier().toString());
		}

		@Test
		@DisplayName("creates ExtensiblePart when upgrades provided")
		void createsExtensiblePartWhenUpgradesProvided() {
			ComponentUpgrades upgrades = ComponentUpgrades.of(List.of(
					ComponentUpgradeSlot.emptyOfType(OpenIdentifier.of("gem_slot"), GEM_TYPE, "Gem socket")
			));

			Component part = ComponentFactory.create(
					PICKAXE_ID,
					Set.of(),
					Map.of(),
					null,
					upgrades,
					false
			);

			assertInstanceOf(CustomizableComponent.class, part);
			assertFalse(part instanceof StructuredComponent);
			assertEquals("forgero:extensible_part", part.getTypeIdentifier().toString());
		}

		@Test
		@DisplayName("creates StructuredExtensiblePart when both structure and upgrades provided")
		void createsStructuredExtensiblePartWhenBothProvided() {
			Component material = ComponentFactory.createStatic(IRON_ID, Set.of(OpenIdentifier.of("material")), Map.of());
			ComponentStructure structure = ComponentStructure.of(List.of(
					ComponentPart.ofType(OpenIdentifier.of("material"), OpenIdentifier.of("material"), "", material)
			));
			ComponentUpgrades upgrades = ComponentUpgrades.of(List.of(
					ComponentUpgradeSlot.emptyOfType(OpenIdentifier.of("gem_slot"), GEM_TYPE, "Gem socket")
			));

			Component part = ComponentFactory.create(
					PICKAXE_ID,
					Set.of(),
					Map.of(),
					structure,
					upgrades,
					false
			);

			assertInstanceOf(StructuredComponent.class, part);
			assertInstanceOf(CustomizableComponent.class, part);
			assertEquals("forgero:structured_extensible_part", part.getTypeIdentifier().toString());
		}
	}

	@Nested
	@DisplayName("Equipment Creation")
	class EquipmentCreation {

		@Test
		@DisplayName("creates StaticEquipment when no structure or upgrades with isEquipment=true")
		void createsStaticEquipmentWhenNoStructureOrUpgrades() {
			Component equipment = ComponentFactory.create(
					PICKAXE_ID,
					Set.of(),
					Map.of(),
					null,
					null,
					true
			);

			assertEquals("forgero:static_equipment", equipment.getTypeIdentifier().toString());
		}

		@Test
		@DisplayName("creates StructuredEquipment when structure provided with isEquipment=true")
		void createsStructuredEquipmentWhenStructureProvided() {
			Component material = ComponentFactory.createStatic(IRON_ID, Set.of(OpenIdentifier.of("pickaxe_head")), Map.of());
			ComponentStructure structure = ComponentStructure.of(List.of(
					ComponentPart.ofType(OpenIdentifier.of("head"), OpenIdentifier.of("pickaxe_head"), "", material)
			));

			Component equipment = ComponentFactory.create(
					PICKAXE_ID,
					Set.of(),
					Map.of(),
					structure,
					null,
					true
			);

			assertInstanceOf(StructuredComponent.class, equipment);
			assertEquals("forgero:structured_equipment", equipment.getTypeIdentifier().toString());
		}

		@Test
		@DisplayName("creates ExtensibleEquipment when upgrades provided with isEquipment=true")
		void createsExtensibleEquipmentWhenUpgradesProvided() {
			ComponentUpgrades upgrades = ComponentUpgrades.of(List.of(
					ComponentUpgradeSlot.emptyOfType(OpenIdentifier.of("binding"), OpenIdentifier.of("binding"), "Binding slot")
			));

			Component equipment = ComponentFactory.create(
					PICKAXE_ID,
					Set.of(),
					Map.of(),
					null,
					upgrades,
					true
			);

			assertInstanceOf(CustomizableComponent.class, equipment);
			assertEquals("forgero:extensible_equipment", equipment.getTypeIdentifier().toString());
		}

		@Test
		@DisplayName("creates StructuredExtensibleEquipment when both provided with isEquipment=true")
		void createsStructuredExtensibleEquipmentWhenBothProvided() {
			Component material = ComponentFactory.createStatic(IRON_ID, Set.of(OpenIdentifier.of("pickaxe_head")), Map.of());
			ComponentStructure structure = ComponentStructure.of(List.of(
					ComponentPart.ofType(OpenIdentifier.of("head"), OpenIdentifier.of("pickaxe_head"), "", material)
			));
			ComponentUpgrades upgrades = ComponentUpgrades.of(List.of(
					ComponentUpgradeSlot.emptyOfType(OpenIdentifier.of("binding"), OpenIdentifier.of("binding"), "Binding slot")
			));

			Component equipment = ComponentFactory.create(
					PICKAXE_ID,
					Set.of(),
					Map.of(),
					structure,
					upgrades,
					true
			);

			assertInstanceOf(StructuredComponent.class, equipment);
			assertInstanceOf(CustomizableComponent.class, equipment);
			assertEquals("forgero:structured_extensible_equipment", equipment.getTypeIdentifier().toString());
		}
	}

	@Nested
	@DisplayName("Static Factory Methods")
	class StaticFactoryMethods {

		@Test
		@DisplayName("createStatic creates basic static component")
		void createStaticCreatesBasicStaticComponent() {
			SimpleAttribute durability = new SimpleAttribute(DefaultAttributes.DURABILITY, 100f);

			Component component = ComponentFactory.createStatic(
					IRON_ID,
					Set.of(METAL_TAG),
					Map.of(Attribute.KEY.key(), List.of(durability))
			);

			assertEquals(IRON_ID, component.id());
			assertTrue(component.getTags().contains(METAL_TAG));
			assertEquals(1, component.properties(Attribute.KEY).size());
		}

		@Test
		@DisplayName("createStaticEquipment creates static equipment")
		void createStaticEquipmentCreatesStaticEquipment() {
			Component equipment = ComponentFactory.createStaticEquipment(
					PICKAXE_ID,
					Set.of(OpenIdentifier.of("tool")),
					Map.of()
			);

			assertEquals("forgero:static_equipment", equipment.getTypeIdentifier().toString());
		}
	}

	@Nested
	@DisplayName("Derive Method")
	class DeriveMethod {

		@Test
		@DisplayName("derive merges properties into new component")
		void deriveMergesPropertiesIntoNewComponent() {
			SimpleAttribute durability = new SimpleAttribute(DefaultAttributes.DURABILITY, 100f);
			Component original = ComponentFactory.createStatic(
					IRON_ID,
					Set.of(METAL_TAG),
					Map.of(Attribute.KEY.key(), List.of(durability))
			);

			SimpleAttribute damage = new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f);
			Component derived = ComponentFactory.derive(original, Map.of(
					Attribute.KEY.key(), List.of(damage)
			));

			assertNotSame(original, derived);
			assertEquals(2, derived.properties(Attribute.KEY).size());
		}

		@Test
		@DisplayName("derive preserves original component immutability")
		void derivePreservesOriginalComponentImmutability() {
			Component original = ComponentFactory.createStatic(
					IRON_ID,
					Set.of(METAL_TAG),
					Map.of()
			);

			SimpleAttribute damage = new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f);
			ComponentFactory.derive(original, Map.of(Attribute.KEY.key(), List.of(damage)));

			assertEquals(0, original.properties(Attribute.KEY).size());
		}
	}

	@Nested
	@DisplayName("Empty Structure and Upgrades Handling")
	class EmptyStructureAndUpgradesHandling {

		@Test
		@DisplayName("empty structure treated as no structure")
		void emptyStructureTreatedAsNoStructure() {
			ComponentStructure emptyStructure = ComponentStructure.of(List.of());

			Component component = ComponentFactory.create(
					IRON_ID,
					Set.of(),
					Map.of(),
					emptyStructure,
					null,
					false
			);

			assertFalse(component instanceof StructuredComponent);
		}

		@Test
		@DisplayName("empty upgrades treated as no upgrades")
		void emptyUpgradesTreatedAsNoUpgrades() {
			ComponentUpgrades emptyUpgrades = ComponentUpgrades.of(List.of());

			Component component = ComponentFactory.create(
					IRON_ID,
					Set.of(),
					Map.of(),
					null,
					emptyUpgrades,
					false
			);

			assertFalse(component instanceof CustomizableComponent);
		}
	}

	@Nested
	@DisplayName("Type Identifier")
	class TypeIdentifierTests {

		@Test
		@DisplayName("all part types have correct type identifiers")
		void allPartTypesHaveCorrectTypeIdentifiers() {
			Component material = ComponentFactory.createStatic(IRON_ID, Set.of(OpenIdentifier.of("material")), Map.of());
			ComponentStructure structure = ComponentStructure.of(List.of(
					ComponentPart.ofType(OpenIdentifier.of("material"), OpenIdentifier.of("material"), "", material)
			));
			ComponentUpgrades upgrades = ComponentUpgrades.of(List.of(
					ComponentUpgradeSlot.emptyOfType(OpenIdentifier.of("slot"), GEM_TYPE, "")
			));

			Component staticComp = ComponentFactory.create(IRON_ID, Set.of(), Map.of(), null, null, false);
			Component structuredPart = ComponentFactory.create(IRON_ID, Set.of(), Map.of(), structure, null, false);
			Component extensiblePart = ComponentFactory.create(IRON_ID, Set.of(), Map.of(), null, upgrades, false);
			Component structuredExtensiblePart = ComponentFactory.create(IRON_ID, Set.of(), Map.of(), structure, upgrades, false);

			assertEquals("forgero:static_component", staticComp.getTypeIdentifier().toString());
			assertEquals("forgero:structured_part", structuredPart.getTypeIdentifier().toString());
			assertEquals("forgero:extensible_part", extensiblePart.getTypeIdentifier().toString());
			assertEquals("forgero:structured_extensible_part", structuredExtensiblePart.getTypeIdentifier().toString());
		}

		@Test
		@DisplayName("all equipment types have correct type identifiers")
		void allEquipmentTypesHaveCorrectTypeIdentifiers() {
			Component material = ComponentFactory.createStatic(IRON_ID, Set.of(OpenIdentifier.of("material")), Map.of());
			ComponentStructure structure = ComponentStructure.of(List.of(
					ComponentPart.ofType(OpenIdentifier.of("material"), OpenIdentifier.of("material"), "", material)
			));
			ComponentUpgrades upgrades = ComponentUpgrades.of(List.of(
					ComponentUpgradeSlot.emptyOfType(OpenIdentifier.of("slot"), GEM_TYPE, "")
			));

			Component staticEquip = ComponentFactory.create(PICKAXE_ID, Set.of(), Map.of(), null, null, true);
			Component structuredEquip = ComponentFactory.create(PICKAXE_ID, Set.of(), Map.of(), structure, null, true);
			Component extensibleEquip = ComponentFactory.create(PICKAXE_ID, Set.of(), Map.of(), null, upgrades, true);
			Component structuredExtensibleEquip = ComponentFactory.create(PICKAXE_ID, Set.of(), Map.of(), structure, upgrades, true);

			assertEquals("forgero:static_equipment", staticEquip.getTypeIdentifier().toString());
			assertEquals("forgero:structured_equipment", structuredEquip.getTypeIdentifier().toString());
			assertEquals("forgero:extensible_equipment", extensibleEquip.getTypeIdentifier().toString());
			assertEquals("forgero:structured_extensible_equipment", structuredExtensibleEquip.getTypeIdentifier().toString());
		}
	}
}
