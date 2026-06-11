package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Debug test to print attribute values for handles and tools.
 * This helps diagnose attribute leak issues.
 */
public class AttributeDebugTest implements ForgeroGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeDebugTest.class);

	private static final List<String> TEST_COMPONENTS = List.of(
			"forgero:iron",
			"forgero:diamond",
			"forgero:oak-handle",
			"forgero:iron-pickaxe_head",
			"forgero:iron-pickaxe",
			"forgero:iron-spear_head",
			"forgero:diamond-spear_head",
			"forgero:iron-spear",
			"forgero:diamond-spear"
	);

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void debug_print_attribute_values(TestContext context) {
		var ctx = ForgeroTestUtils.forgero(context);

		LOGGER.debug("=== ATTRIBUTE DEBUG TEST - Component Values ===");

		for (String componentId : TEST_COMPONENTS) {
			var componentOpt = ctx.component(componentId);
			if (componentOpt.isEmpty()) {
				LOGGER.debug("MISSING component: {}", componentId);
				continue;
			}

			Component component = componentOpt.get();
			printComponentAttributes(componentId, component);
		}

		LOGGER.debug("=== END ATTRIBUTE DEBUG TEST ===");

		context.complete();
	}

	private void printComponentAttributes(String id, Component component) {
		AttributeQueryResult attributes = AttributeEngine.resolveAttributes(component);

		LOGGER.debug("--- Component: {} ---", id);

		float attackDamage = attributes.getValue(DefaultAttributes.ATTACK_DAMAGE);
		float attackSpeed = attributes.getValue(DefaultAttributes.ATTACK_SPEED);
		float durability = attributes.getValue(DefaultAttributes.DURABILITY);
		float miningSpeed = attributes.getValue(DefaultAttributes.MINING_SPEED);
		float miningLevel = attributes.getValue(DefaultAttributes.MINING_LEVEL);
		float armor = attributes.getValue(DefaultAttributes.ARMOR);
		float armorToughness = attributes.getValue(DefaultAttributes.ARMOR_TOUGHNESS);

		// Only log non-zero values (to focus on what's actually set)
		LOGGER.debug("  Resolved Attributes:");
		if (attackDamage != 0) LOGGER.debug("    attack_damage: {}", attackDamage);
		if (attackSpeed != 0) LOGGER.debug("    attack_speed: {}", attackSpeed);
		if (durability != 0) LOGGER.debug("    durability: {}", durability);
		if (miningSpeed != 0) LOGGER.debug("    mining_speed: {}", miningSpeed);
		if (miningLevel != 0) LOGGER.debug("    mining_level: {}", miningLevel);
		if (armor != 0) LOGGER.debug("    armor: {} ***UNEXPECTED FOR TOOLS***", armor);
		if (armorToughness != 0) LOGGER.debug("    armor_toughness: {} ***UNEXPECTED FOR TOOLS***", armorToughness);

		// Also log raw attributes from component tree
		LOGGER.debug("  Raw Attributes (before condition filtering):");
		printRawAttributes(component, "    ");
	}

	private void printRawAttributes(Component component, String indent) {
		var attrs = component.properties(com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY);
		for (var attr : attrs) {
			String conditioned = attr.condition().isPresent() ? " (conditioned)" : " (NO CONDITION!)";
			LOGGER.debug("{}{} = {}{}", indent, attr.type().path(), attr.value(), conditioned);
		}

		// Recurse into children
		for (Component child : component.getChildren()) {
			LOGGER.debug("{}Child: {}", indent, child.id());
			printRawAttributes(child, indent + "  ");
		}
	}
}
