package com.sigmundgranaas.forgero.core.attribute;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.component.api.Component;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to validate that static components (like vanilla tools) can have attributes
 * and that those attributes are accessible via both direct property queries and
 * the attribute resolution system.
 *
 * This isolates whether the attribute system works in core, separate from JSON loading.
 */
class StaticComponentAttributeTest extends ForgeroTest {

	/**
	 * Test 1: Can a static component have attributes directly?
	 * This tests if Component.properties(Attribute.KEY) returns attributes.
	 */
	@Test
	void staticComponentCanHaveAttributesDirect() {
		Component ironPickaxe = part("iron_pickaxe")
				.withTag("pickaxe")
				.withTag("vanilla_tool")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 4f)
				.withAttribute(DefaultAttributes.DURABILITY, 250f)
				.withAttribute(DefaultAttributes.MINING_SPEED, 6f)
				.withAttribute(DefaultAttributes.MINING_LEVEL, 2f)
				.build();

		// Query attributes directly from component
		List<? extends Attribute> attributes = ironPickaxe.properties(Attribute.KEY);

		assertNotNull(attributes, "Attributes list should not be null");
		assertFalse(attributes.isEmpty(), "Static component should have attributes");
		assertEquals(4, attributes.size(), "Should have 4 attributes");

		// Verify each attribute is present
		boolean hasAttackDamage = attributes.stream()
				.anyMatch(attr -> attr.type().equals(DefaultAttributes.ATTACK_DAMAGE) && attr.value() == 4f);
		boolean hasDurability = attributes.stream()
				.anyMatch(attr -> attr.type().equals(DefaultAttributes.DURABILITY) && attr.value() == 250f);
		boolean hasMiningSpeed = attributes.stream()
				.anyMatch(attr -> attr.type().equals(DefaultAttributes.MINING_SPEED) && attr.value() == 6f);
		boolean hasMiningLevel = attributes.stream()
				.anyMatch(attr -> attr.type().equals(DefaultAttributes.MINING_LEVEL) && attr.value() == 2f);

		assertTrue(hasAttackDamage, "Should have attack_damage attribute with value 4");
		assertTrue(hasDurability, "Should have durability attribute with value 250");
		assertTrue(hasMiningSpeed, "Should have mining_speed attribute with value 6");
		assertTrue(hasMiningLevel, "Should have mining_level attribute with value 2");
	}

	/**
	 * Test 2: Can a static component's attributes be resolved via AttributeEngine?
	 * This tests the full resolution pipeline.
	 */
	@Test
	void staticComponentAttributesResolveCorrectly() {
		Component ironPickaxe = part("iron_pickaxe")
				.withTag("pickaxe")
				.withTag("vanilla_tool")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 4f)
				.withAttribute(DefaultAttributes.DURABILITY, 250f)
				.withAttribute(DefaultAttributes.MINING_SPEED, 6f)
				.withAttribute(DefaultAttributes.MINING_LEVEL, 2f)
				.build();

		AttributeQueryResult result = resolveAttributes(ironPickaxe);

		assertEquals(4f, result.getValue(DefaultAttributes.ATTACK_DAMAGE),
				"Attack damage should resolve to 4");
		assertEquals(250f, result.getValue(DefaultAttributes.DURABILITY),
				"Durability should resolve to 250");
		assertEquals(6f, result.getValue(DefaultAttributes.MINING_SPEED),
				"Mining speed should resolve to 6");
		assertEquals(2f, result.getValue(DefaultAttributes.MINING_LEVEL),
				"Mining level should resolve to 2");
	}

	/**
	 * Test 3: Test all vanilla tool types as static components
	 * This replicates the exact scenario from VanillaToolParityTest
	 */
	@Test
	void allVanillaToolTypesHaveAttributes() {
		// Iron Pickaxe
		Component ironPickaxe = part("iron_pickaxe")
				.withTag("pickaxe")
				.withTag("vanilla_tool")
				.withAttribute(DefaultAttributes.DURABILITY, 250f)
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 4f)
				.withAttribute(DefaultAttributes.MINING_SPEED, 6f)
				.withAttribute(DefaultAttributes.MINING_LEVEL, 2f)
				.build();

		// Iron Sword
		Component ironSword = part("iron_sword")
				.withTag("sword")
				.withTag("vanilla_tool")
				.withAttribute(DefaultAttributes.DURABILITY, 250f)
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 6f)
				.withAttribute(DefaultAttributes.ATTACK_SPEED, 1.6f)
				.build();

		// Diamond Pickaxe
		Component diamondPickaxe = part("diamond_pickaxe")
				.withTag("pickaxe")
				.withTag("vanilla_tool")
				.withAttribute(DefaultAttributes.DURABILITY, 1561f)
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f)
				.withAttribute(DefaultAttributes.MINING_SPEED, 8f)
				.withAttribute(DefaultAttributes.MINING_LEVEL, 3f)
				.build();

		// Gold Pickaxe
		Component goldPickaxe = part("golden_pickaxe")
				.withTag("pickaxe")
				.withTag("vanilla_tool")
				.withAttribute(DefaultAttributes.DURABILITY, 32f)
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 2f)
				.withAttribute(DefaultAttributes.MINING_SPEED, 12f)
				.withAttribute(DefaultAttributes.MINING_LEVEL, 0f)
				.build();

		// Verify all tools have attributes
		assertFalse(ironPickaxe.properties(Attribute.KEY).isEmpty(), "Iron pickaxe should have attributes");
		assertFalse(ironSword.properties(Attribute.KEY).isEmpty(), "Iron sword should have attributes");
		assertFalse(diamondPickaxe.properties(Attribute.KEY).isEmpty(), "Diamond pickaxe should have attributes");
		assertFalse(goldPickaxe.properties(Attribute.KEY).isEmpty(), "Gold pickaxe should have attributes");

		// Verify attributes resolve correctly
		AttributeQueryResult ironPickaxeResult = resolveAttributes(ironPickaxe);
		assertEquals(250f, ironPickaxeResult.getValue(DefaultAttributes.DURABILITY));
		assertEquals(4f, ironPickaxeResult.getValue(DefaultAttributes.ATTACK_DAMAGE));

		AttributeQueryResult ironSwordResult = resolveAttributes(ironSword);
		assertEquals(250f, ironSwordResult.getValue(DefaultAttributes.DURABILITY));
		assertEquals(6f, ironSwordResult.getValue(DefaultAttributes.ATTACK_DAMAGE));

		AttributeQueryResult diamondResult = resolveAttributes(diamondPickaxe);
		assertEquals(1561f, diamondResult.getValue(DefaultAttributes.DURABILITY));
		assertEquals(8f, diamondResult.getValue(DefaultAttributes.MINING_SPEED));

		AttributeQueryResult goldResult = resolveAttributes(goldPickaxe);
		assertEquals(32f, goldResult.getValue(DefaultAttributes.DURABILITY));
		assertEquals(12f, goldResult.getValue(DefaultAttributes.MINING_SPEED));
	}

	/**
	 * Test 4: Empty component should have no attributes
	 * This validates the baseline - components without attributes work correctly
	 */
	@Test
	void emptyComponentHasNoAttributes() {
		Component emptyPart = part("empty_part")
				.withTag("empty")
				.build();

		List<? extends Attribute> attributes = emptyPart.properties(Attribute.KEY);

		assertNotNull(attributes, "Attributes list should not be null");
		assertTrue(attributes.isEmpty(), "Empty component should have no attributes");
	}

	/**
	 * Test 5: Component with only some attributes
	 * Tests partial attribute assignment
	 */
	@Test
	void partialAttributesWork() {
		Component partialTool = part("partial_tool")
				.withTag("tool")
				.withAttribute(DefaultAttributes.ATTACK_DAMAGE, 5f)
				// Note: No other attributes
				.build();

		List<? extends Attribute> attributes = partialTool.properties(Attribute.KEY);

		assertFalse(attributes.isEmpty(), "Component with one attribute should not be empty");
		assertEquals(1, attributes.size(), "Should have exactly 1 attribute");

		AttributeQueryResult result = resolveAttributes(partialTool);
		assertEquals(5f, result.getValue(DefaultAttributes.ATTACK_DAMAGE));
	}
}
