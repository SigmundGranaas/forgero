package com.sigmundgranaas.forgero.core.state.identity;

import static com.sigmundgranaas.forgero.core.state.identity.Condition.ALWAYS;
import static com.sigmundgranaas.forgero.core.state.identity.ModificationRuleBuilder.builder;

import com.sigmundgranaas.forgero.core.type.Type;

public class DefaultRules {
	public static Condition swordBladeType = Condition.type(Type.SWORD_BLADE);
	public static Condition handleType = Condition.type(Type.HANDLE);

	public static Condition toolPartHead = Condition.type(Type.TOOL_PART_HEAD);

	public static Condition schematicType = Condition.type(Type.SCHEMATIC);

	public static ModificationRuleBuilder blade = builder()
			.when(swordBladeType)
			.remove("_blade");

	public static ModificationRuleBuilder head = builder()
			.when(toolPartHead)
			.remove("_head");

	public static ModificationRuleBuilder sword = builder()
			.when(Condition.type(Type.SWORD_BLADE))
			.replaceElement("_blade", "sword");

	public static ModificationRuleBuilder pickaxe = builder()
			.when(Condition.type(Type.PICKAXE_HEAD))
			.replaceElement("_head", "pickaxe");
	
	public static ModificationRuleBuilder shovel = builder()
			.when(Condition.type(Type.SHOVEL_HEAD))
			.replaceElement("_head", "shovel");

	public static ModificationRuleBuilder hoe = builder()
			.when(Condition.type(Type.HOE_HEAD))
			.replaceElement("_head", "hoe");

	public static ModificationRuleBuilder axe = builder()
			.when(Condition.type(Type.AXE_HEAD))
			.replaceElement("_head", "axe");

	public static ModificationRuleBuilder handle = builder()
			.when(handleType)
			.ignore();

	public static ModificationRuleBuilder schematic = builder()
			.when(schematicType)
			.takeElement(0);

	public static ModificationRuleBuilder defaultRule = builder().when(ALWAYS);
}
