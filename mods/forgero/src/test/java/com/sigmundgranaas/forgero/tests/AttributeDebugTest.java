package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import java.util.List;

/**
 * Debug test to print attribute values for handles and tools.
 * This helps diagnose attribute leak issues.
 */
public class AttributeDebugTest implements ForgeroGameTest {

	private static final List<String> TEST_COMPONENTS = List.of(
			// Handles
			"forgero:oak-handle",
			"forgero:iron-handle",
			"forgero:netherite-handle",
			"forgero:stone-handle",
			// Pickaxe heads
			"forgero:iron-pickaxe_head",
			"forgero:netherite-pickaxe_head",
			"forgero:stone-pickaxe_head",
			// Full pickaxes
			"forgero:iron-pickaxe",
			"forgero:netherite-pickaxe",
			"forgero:stone-pickaxe"
	);

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void debug_print_attribute_values(TestContext context) {
		var ctx = forgero(context);

		System.out.println("\n========================================");
		System.out.println("ATTRIBUTE DEBUG TEST - Component Values");
		System.out.println("========================================\n");

		for (String componentId : TEST_COMPONENTS) {
			var componentOpt = ctx.component(componentId);
			if (componentOpt.isEmpty()) {
				System.out.println("MISSING: " + componentId);
				continue;
			}

			Component component = componentOpt.get();
			printComponentAttributes(componentId, component);
		}

		System.out.println("\n========================================");
		System.out.println("END ATTRIBUTE DEBUG TEST");
		System.out.println("========================================\n");

		context.complete();
	}

	private void printComponentAttributes(String id, Component component) {
		AttributeQueryResult attributes = services().resolver().resolve(component, new AttributeEngine());

		System.out.println("--- " + id + " ---");

		float attackDamage = attributes.getValue(DefaultAttributes.ATTACK_DAMAGE);
		float attackSpeed = attributes.getValue(DefaultAttributes.ATTACK_SPEED);
		float durability = attributes.getValue(DefaultAttributes.DURABILITY);
		float miningSpeed = attributes.getValue(DefaultAttributes.MINING_SPEED);
		float miningLevel = attributes.getValue(DefaultAttributes.MINING_LEVEL);
		float armor = attributes.getValue(DefaultAttributes.ARMOR);
		float armorToughness = attributes.getValue(DefaultAttributes.ARMOR_TOUGHNESS);

		// Only print non-zero values (to focus on what's actually set)
		System.out.println("  Resolved Attributes:");
		if (attackDamage != 0) System.out.printf("    attack_damage: %.2f%n", attackDamage);
		if (attackSpeed != 0) System.out.printf("    attack_speed: %.2f%n", attackSpeed);
		if (durability != 0) System.out.printf("    durability: %.2f%n", durability);
		if (miningSpeed != 0) System.out.printf("    mining_speed: %.2f%n", miningSpeed);
		if (miningLevel != 0) System.out.printf("    mining_level: %.2f%n", miningLevel);
		if (armor != 0) System.out.printf("    armor: %.2f ***UNEXPECTED FOR TOOLS***%n", armor);
		if (armorToughness != 0) System.out.printf("    armor_toughness: %.2f ***UNEXPECTED FOR TOOLS***%n", armorToughness);

		// Also print raw attributes from component tree
		System.out.println("  Raw Attributes (before condition filtering):");
		printRawAttributes(component, "    ");

		System.out.println();
	}

	private void printRawAttributes(Component component, String indent) {
		var attrs = component.properties(com.sigmundgranaas.forgero.core.attribute.api.Attribute.KEY);
		for (var attr : attrs) {
			String conditioned = attr.condition().isPresent() ? " (conditioned)" : " (NO CONDITION!)";
			System.out.println(indent + attr.type().path() + " = " + attr.value() + conditioned);
		}

		// Recurse into children
		for (Component child : component.getChildren()) {
			System.out.println(indent + "Child: " + child.id());
			printRawAttributes(child, indent + "  ");
		}
	}
}
