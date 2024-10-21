package com.sigmundgranaas.forgero.bow.item;

import static com.sigmundgranaas.forgero.core.api.identity.ModificationRuleBuilder.builder;

import com.sigmundgranaas.forgero.core.api.identity.Condition;
import com.sigmundgranaas.forgero.core.api.identity.ModificationRuleBuilder;
import com.sigmundgranaas.forgero.core.type.Type;

public class NamingRules {



	public static Condition bowLimbType = Condition.type(Type.BOW_LIMB);

	public static ModificationRuleBuilder bowLimb = builder()
			.when(bowLimbType)
			.replaceElement("bow_limb", "bow");

	public static Condition arrowHeadType = Condition.type(Type.ARROW_HEAD);

	public static ModificationRuleBuilder arrowHead = builder()
			.when(arrowHeadType)
			.replaceElement("arrow_head", "arrow");

	public static Condition stringType = Condition.type(Type.STRING);

	public static ModificationRuleBuilder string = builder()
			.when(stringType)
			.ignore();

	public static Condition featherType = Condition.type("FEATHER");

	public static ModificationRuleBuilder feather = builder()
			.when(featherType)
			.ignore();
}
