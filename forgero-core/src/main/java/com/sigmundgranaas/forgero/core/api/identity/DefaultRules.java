package com.sigmundgranaas.forgero.core.api.identity;

import com.sigmundgranaas.forgero.core.type.Type;

/**
 * A collection of default rules that validate a lot of the default naming conventions in Forgero.
 */
public class DefaultRules {
	public static Condition swordBladeType = Condition.type(Type.SWORD_BLADE);
	public static Condition handleType = Condition.type(Type.HANDLE);

	public static Condition toolPartHead = Condition.type(Type.TOOL_PART_HEAD);

	public static Condition schematicType = Condition.type(Type.SCHEMATIC);

	public static ModificationRuleBuilder blade = ModificationRuleBuilder.builder()
			.when(swordBladeType)
			.remove("_blade");

	public static ModificationRuleBuilder head = ModificationRuleBuilder.builder()
			.when(toolPartHead)
			.remove("_head");

	public static ModificationRuleBuilder sword = ModificationRuleBuilder.builder()
			.when(Condition.type(Type.SWORD_BLADE))
			.replaceElement("_blade", "sword");

	public static ModificationRuleBuilder pickaxe = ModificationRuleBuilder.builder()
			.when(Condition.type(Type.PICKAXE_HEAD))
			.replaceElement("_head", "pickaxe");

	public static ModificationRuleBuilder shovel = ModificationRuleBuilder.builder()
			.when(Condition.type(Type.SHOVEL_HEAD))
			.replaceElement("_head", "shovel");

	public static ModificationRuleBuilder hoe = ModificationRuleBuilder.builder()
			.when(Condition.type(Type.HOE_HEAD))
			.replaceElement("_head", "hoe");

	public static ModificationRuleBuilder axe = ModificationRuleBuilder.builder()
			.when(Condition.type(Type.AXE_HEAD))
			.replaceElement("_head", "axe");

	public static ModificationRuleBuilder handle = ModificationRuleBuilder.builder()
			.when(handleType)
			.ignore();

	public static ModificationRuleBuilder schematic = ModificationRuleBuilder.builder()
			.when(schematicType)
			.takeElement(0);

	public static ModificationRuleBuilder defaultRule = ModificationRuleBuilder.builder().when(Condition.ALWAYS);
}
