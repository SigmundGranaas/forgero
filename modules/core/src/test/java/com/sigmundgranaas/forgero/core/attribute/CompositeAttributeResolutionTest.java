package com.sigmundgranaas.forgero.core.attribute;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeContext;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests attribute resolution for composite components built from materials.
 * <p>
 * This test validates the exact scenario causing gametest failures:
 * - Materials have attributes (iron: 240 durability, diamond: 1550 durability)
 * - Composite tools (pickaxes) should inherit material attributes
 * - Both iron and diamond pickaxes were showing 50 durability instead of material values
 * <p>
 * This test reproduces the problem at the simplest level to identify the root cause.
 */
class CompositeAttributeResolutionTest extends ForgeroTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(CompositeAttributeResolutionTest.class);
	private Resolver resolver;

	@BeforeEach
	void setUp() {
		resolver = resolver();
	}

	/**
	 * Test 1: Simple material with attributes
	 * Validates that materials can have attributes directly.
	 */
	@Test
	void materialHasAttributes() {
		Component ironMaterial = part("iron")
				.withTag("materials/types/metal")
				.withTag("materials/roles/tool_material")
				.withAttribute(DefaultAttributes.DURABILITY, 240f)
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 4f)
				.withAttribute(DefaultAttributes.MINING_SPEED, 6f)
				.withAttribute(DefaultAttributes.MINING_LEVEL, 2f)
				.build();

		AttributeQueryResult result = resolver.resolve(ironMaterial, attributeEngine());

		assertEquals(240f, result.getValue(DefaultAttributes.DURABILITY),
				"Iron material should have 240 durability");
		assertEquals(4f, result.getValue(DefaultAttributes.ATTACK_DAMAGE),
				"Iron material should have 4 attack damage");
	}

	/**
	 * Test 2: Composite part made from material
	 * Tests that a pickaxe head made from iron inherits iron's attributes.
	 */
	@Test
	void compositePartInheritsMaterialAttributes() {
		// Create iron material with attributes
		Component ironMaterial = part("iron")
				.withTag("materials/types/metal")
				.withTag("materials/roles/tool_material")
				.withAttribute(DefaultAttributes.DURABILITY, 240f)
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 4f)
				.withAttribute(DefaultAttributes.MINING_SPEED, 6f)
				.withAttribute(DefaultAttributes.MINING_LEVEL, 2f)
				.build();

		// Create pickaxe head from iron material
		Component ironPickaxeHead = part("iron-pickaxe_head")
				.withTag("parts/types/pickaxe_head")
				.withTag("parts/categories/head")
				.withStructureSlot(structureSlot("material", id("material_slot"), ironMaterial))
				.build();

		AttributeQueryResult result = resolver.resolve(ironPickaxeHead, attributeEngine());

		// The head should inherit material attributes
		float durability = result.getValue(DefaultAttributes.DURABILITY);
		float attackDamage = result.getValue(DefaultAttributes.ATTACK_DAMAGE);

		LOGGER.debug("Iron pickaxe head attributes: durability={}, attackDamage={}", durability, attackDamage);

		assertTrue(durability >= 240f,
				String.format("Iron pickaxe head should inherit iron's 240 durability, got %f", durability));
		assertTrue(attackDamage >= 4f,
				String.format("Iron pickaxe head should inherit iron's 4 attack damage, got %f", attackDamage));
	}

	/**
	 * Test 3: Full composite tool from parts
	 * Tests that a complete pickaxe (head + handle) inherits attributes from its parts.
	 */
	@Test
	void compositeToolInheritsAttributesFromParts() {
		// Create iron material
		Component ironMaterial = part("iron")
				.withTag("materials/types/metal")
				.withTag("materials/roles/tool_material")
				.withAttribute(DefaultAttributes.DURABILITY, 240f)
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 4f)
				.withAttribute(DefaultAttributes.MINING_SPEED, 6f)
				.withAttribute(DefaultAttributes.MINING_LEVEL, 2f)
				.build();

		// Create oak material for handle
		Component oakMaterial = part("oak")
				.withTag("materials/types/wood")
				.withTag("materials/roles/tool_material")
				.withAttribute(DefaultAttributes.DURABILITY, 60f)
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 1f)
				.build();

		// Create pickaxe head from iron
		Component ironPickaxeHead = part("iron-pickaxe_head")
				.withTag("parts/types/pickaxe_head")
				.withStructureSlot(structureSlot("material", id("material_slot"), ironMaterial))
				.build();

		// Create handle from oak
		Component oakHandle = part("oak-handle")
				.withTag("parts/types/handle")
				.withStructureSlot(structureSlot("material", id("material_slot"), oakMaterial))
				.build();

		// Create complete pickaxe from head + handle
		Component ironPickaxe = tool("iron-pickaxe")
				.withTag("tools/pickaxe")
				.withPart(ironPickaxeHead, "head_slot", id("parts/types/pickaxe_head"))
				.withPart(oakHandle, "handle_slot", id("parts/types/handle"))
				.withAttribute(DefaultAttributes.ATTACK_SPEED, -2.8f) // Tool-level attribute
				.build();

		AttributeQueryResult result = resolver.resolve(ironPickaxe, attributeEngine());

		// Should get attributes from both head (iron) and handle (oak)
		float durability = result.getValue(DefaultAttributes.DURABILITY);
		float attackDamage = result.getValue(DefaultAttributes.ATTACK_DAMAGE);

		LOGGER.debug("Iron pickaxe attributes: durability={}, attackDamage={}", durability, attackDamage);

		assertTrue(durability >= 240f,
				String.format("Iron pickaxe should have at least iron's 240 durability, got %f", durability));
		assertTrue(attackDamage >= 4f,
				String.format("Iron pickaxe should have at least iron's 4 attack damage, got %f", attackDamage));
	}

	/**
	 * Test 4: Context-aware attribute composition (mimics real JSON)
	 * This test uses context-aware attributes like the actual JSON files do.
	 * Materials should provide base values with part-composite context,
	 * and shapes should provide multipliers with part-composite context.
	 */
	@Test
	void contextAwareAttributeComposition() {
		// Create iron material with part-composite context (like JSON)
		Attribute ironDurabilityBase = SimpleAttribute.withContext(
				DefaultAttributes.DURABILITY, 240f, AdditionOperator.getInstance(),
				AttributeContext.PART_COMPOSITE);
		Attribute ironMiningSpeedBase = SimpleAttribute.withContext(
				DefaultAttributes.MINING_SPEED, 6f, AdditionOperator.getInstance(),
				AttributeContext.PART_COMPOSITE);

		Component ironMaterial = part("iron")
				.withTag("materials/types/metal")
				.withAttribute(ironDurabilityBase)
				.withAttribute(ironMiningSpeedBase)
				.build();

		// Create pickaxe_head shape with part-composite context multipliers (like JSON)
		Attribute durabilityMult = SimpleAttribute.withContext(
				DefaultAttributes.DURABILITY, 1.0f, MultiplicationOperator.getInstance(),
				AttributeContext.PART_COMPOSITE);
		Attribute miningSpeedMult = SimpleAttribute.withContext(
				DefaultAttributes.MINING_SPEED, 1.2f, MultiplicationOperator.getInstance(),
				AttributeContext.PART_COMPOSITE);

		Component pickaxeHeadShape = part("pickaxe_head_shape")
				.withTag("shapes/pickaxe_head")
				.withAttribute(durabilityMult)
				.withAttribute(miningSpeedMult)
				.build();

		// Create pickaxe head from iron material + pickaxe head shape
		Component ironPickaxeHead = part("iron-pickaxe_head")
				.withTag("parts/types/pickaxe_head")
				.withStructureSlot(structureSlot("material", id("material_slot"), ironMaterial))
				.withStructureSlot(structureSlot("shape", id("shape_slot"), pickaxeHeadShape))
				.build();

		AttributeQueryResult result = resolver.resolve(ironPickaxeHead, attributeEngine());

		float durability = result.getValue(DefaultAttributes.DURABILITY);
		float miningSpeed = result.getValue(DefaultAttributes.MINING_SPEED);

		LOGGER.debug("Context-aware pickaxe head attributes: durability={}, miningSpeed={}", durability, miningSpeed);

		// Should compose: 240 × 1.0 = 240 for durability
		assertEquals(240f, durability, 0.01f,
				String.format("Pickaxe head should compose material base (240) × shape mult (1.0), got %f", durability));
		// Should compose: 6 × 1.2 = 7.2 for mining_speed
		assertEquals(7.2f, miningSpeed, 0.01f,
				String.format("Pickaxe head should compose material base (6) × shape mult (1.2), got %f", miningSpeed));
	}

	/**
	 * Test 5: Different materials give different results
	 * This is the exact scenario from the failing gametest:
	 * - Iron pickaxe and diamond pickaxe should have different durability
	 */
	@Test
	void differentMaterialsGiveDifferentToolAttributes() {
		// Create iron material
		Component ironMaterial = part("iron")
				.withTag("materials/types/metal")
				.withAttribute(DefaultAttributes.DURABILITY, 240f)
				.withAttribute(DefaultAttributes.MINING_SPEED, 6f)
				.build();

		// Create diamond material
		Component diamondMaterial = part("diamond")
				.withTag("materials/types/mineral")
				.withAttribute(DefaultAttributes.DURABILITY, 1550f)
				.withAttribute(DefaultAttributes.MINING_SPEED, 8f)
				.build();

		// Create oak for handles
		Component oakMaterial = part("oak")
				.withTag("materials/types/wood")
				.withAttribute(DefaultAttributes.DURABILITY, 60f)
				.build();

		// Build iron pickaxe
		Component ironHead = part("iron-pickaxe_head")
				.withTag("parts/types/pickaxe_head")
				.withStructureSlot(structureSlot("material", id("material_slot"), ironMaterial))
				.build();
		Component oakHandle1 = part("oak-handle-1")
				.withTag("parts/types/handle")
				.withStructureSlot(structureSlot("material", id("material_slot"), oakMaterial))
				.build();
		Component ironPickaxe = tool("iron-pickaxe")
				.withTag("tools/pickaxe")
				.withPart(ironHead, "head_slot", id("parts/types/pickaxe_head"))
				.withPart(oakHandle1, "handle_slot", id("parts/types/handle"))
				.build();

		// Build diamond pickaxe
		Component diamondHead = part("diamond-pickaxe_head")
				.withTag("parts/types/pickaxe_head")
				.withStructureSlot(structureSlot("material", id("material_slot"), diamondMaterial))
				.build();
		Component oakHandle2 = part("oak-handle-2")
				.withTag("parts/types/handle")
				.withStructureSlot(structureSlot("material", id("material_slot"), oakMaterial))
				.build();
		Component diamondPickaxe = tool("diamond-pickaxe")
				.withTag("tools/pickaxe")
				.withPart(diamondHead, "head_slot", id("parts/types/pickaxe_head"))
				.withPart(oakHandle2, "handle_slot", id("parts/types/handle"))
				.build();

		// Resolve attributes
		AttributeQueryResult ironResult = resolver.resolve(ironPickaxe, attributeEngine());
		AttributeQueryResult diamondResult = resolver.resolve(diamondPickaxe, attributeEngine());

		float ironDurability = ironResult.getValue(DefaultAttributes.DURABILITY);
		float diamondDurability = diamondResult.getValue(DefaultAttributes.DURABILITY);

		LOGGER.debug("Material comparison: ironPickaxeDurability={}, diamondPickaxeDurability={}", ironDurability, diamondDurability);

		// THIS IS THE KEY ASSERTION THAT FAILS IN GAMETEST
		assertNotEquals(50f, ironDurability,
				"Iron pickaxe should not have placeholder value of 50");
		assertNotEquals(50f, diamondDurability,
				"Diamond pickaxe should not have placeholder value of 50");

		assertTrue(diamondDurability > ironDurability,
				String.format("Diamond pickaxe durability (%f) should be > iron (%f)",
						diamondDurability, ironDurability));
	}
}
