package com.sigmundgranaas.forgero.bow.item;

import static com.sigmundgranaas.forgero.bow.item.NamingRules.*;
import static com.sigmundgranaas.forgero.core.property.CalculationOrder.BASE;
import static com.sigmundgranaas.forgero.core.property.NumericOperation.ADDITION;
import static com.sigmundgranaas.forgero.core.type.Type.SCHEMATIC;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.api.v0.identity.DefaultRules;
import com.sigmundgranaas.forgero.core.api.v0.identity.ModificationRuleRegistry;
import com.sigmundgranaas.forgero.core.api.v0.identity.sorting.SortingRule;
import com.sigmundgranaas.forgero.core.api.v0.identity.sorting.SortingRuleRegistry;
import com.sigmundgranaas.forgero.core.property.Attribute;
import com.sigmundgranaas.forgero.core.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.state.Ingredient;
import com.sigmundgranaas.forgero.core.state.NameCompositor;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.type.SimpleType;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.SchematicMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NamingRuleTest {
	public static NameCompositor compositor() {
		SortingRuleRegistry sorting = SortingRuleRegistry.local();
		sorting.registerRule("forgero:schematic", SortingRule.of(Type.SCHEMATIC, 20));
		sorting.registerRule("forgero:material", SortingRule.of(Type.MATERIAL, 10));
		sorting.registerRule("forgero:part", SortingRule.of(Type.PART, 30));

		ModificationRuleRegistry modification = ModificationRuleRegistry.local();
		modification.registerRule("forgero:handle", DefaultRules.handle.build());
		modification.registerRule("forgero:arrow_head", arrowHead.build());
		modification.registerRule("forgero:feather", feather.build());

		modification.registerRule("forgero:schematic", DefaultRules.schematic.build());
		modification.registerRule("forgero:bow_limb", bowLimb.build());
		modification.registerRule("forgero:string", string.build());
		return new NameCompositor(modification, sorting);
	}

	private NameCompositor compositor;
	public static Attribute ATTACK_DAMAGE_1 = new AttributeBuilder(AttackDamage.KEY)
			.applyOrder(BASE)
			.applyOperation(ADDITION)
			.applyValue(1)
			.build();

	public static State OAK = Ingredient.of("oak", Type.WOOD, List.of(ATTACK_DAMAGE_1));
	public static State IRON = Ingredient.of("iron", Type.METAL, List.of(ATTACK_DAMAGE_1));

	public static Type ARROW_HEAD_SCHEMATIC_TYPE = new SimpleType("ARROW_HEAD_SCHEMATIC", Optional.of(SCHEMATIC), new SchematicMatcher());
	public static Type BOW_LIMB_SCHEMATIC_TYPE = new SimpleType("BOW_LIMB_SCHEMATIC", Optional.of(SCHEMATIC), new SchematicMatcher());
	public static Type HANDLE_SCHEMATIC_TYPE = new SimpleType("HANDLE_SCHEMATIC", Optional.of(SCHEMATIC), new SchematicMatcher());

	public static State HANDLE_SCHEMATIC = Ingredient.of("handle-schematic", HANDLE_SCHEMATIC_TYPE, List.of(ATTACK_DAMAGE_1));
	public static State ARROW_HEAD_SCHEMATIC = Ingredient.of("arrow_head-schematic", ARROW_HEAD_SCHEMATIC_TYPE, List.of(ATTACK_DAMAGE_1));
	public static State MASTER_HEAD_SCHEMATIC = Ingredient.of("mastercrafted_arrow_head-schematic", ARROW_HEAD_SCHEMATIC_TYPE, List.of(ATTACK_DAMAGE_1));

	public static State REFINED_BOW_LIMB_SCHEMATIC = Ingredient.of("refined_bow_limb-schematic", BOW_LIMB_SCHEMATIC_TYPE, List.of(ATTACK_DAMAGE_1));
	public static State BOW_LIMB_SCHEMATIC = Ingredient.of("bow_limb-schematic", BOW_LIMB_SCHEMATIC_TYPE, List.of(ATTACK_DAMAGE_1));
	public static State FEATHER = Ingredient.of("feather", Type.of("FEATHER"), List.of(ATTACK_DAMAGE_1));
	public static State STRING = Ingredient.of("string", Type.of("STRING"), List.of(ATTACK_DAMAGE_1));

	public static State HANDLE = Construct.builder()
			.addIngredient(OAK)
			.addIngredient(HANDLE_SCHEMATIC)
			.type(Type.HANDLE)
			.compositor(compositor())
			.build();

	public static State ARROW_HEAD = Construct.builder()
			.addIngredient(IRON)
			.addIngredient(ARROW_HEAD_SCHEMATIC)
			.type(Type.ARROW_HEAD)
			.compositor(compositor())
			.build();

	public static State ARROW_HEAD_MASTER = Construct.builder()
			.addIngredient(IRON)
			.addIngredient(MASTER_HEAD_SCHEMATIC)
			.type(Type.ARROW_HEAD)
			.compositor(compositor())
			.build();

	public static State BOW_LIMB_REFINED = Construct.builder()
			.addIngredient(OAK)
			.addIngredient(REFINED_BOW_LIMB_SCHEMATIC)
			.type(Type.BOW_LIMB)
			.compositor(compositor())
			.build();

	public static State BOW_LIMB = Construct.builder()
			.addIngredient(OAK)
			.addIngredient(BOW_LIMB_SCHEMATIC)
			.type(Type.BOW_LIMB)
			.compositor(compositor())
			.build();


	@BeforeEach
	public void prepareRegistry() {
		compositor = compositor();
	}

	@Test
	public void testArrow() {
		List<State> arrow = List.of(ARROW_HEAD, HANDLE, FEATHER);
		Assertions.assertEquals("iron-arrow", compositor.compositeName(arrow));
	}

	@Test
	public void testMastercraftedArrow() {
		List<State> arrow = List.of(ARROW_HEAD_MASTER, HANDLE, FEATHER);
		Assertions.assertEquals("iron-arrow", compositor.compositeName(arrow));
	}

	@Test
	public void testBow() {
		List<State> parts = List.of(BOW_LIMB, STRING);
		Assertions.assertEquals("oak-bow", compositor.compositeName(parts));
	}

	@Test
	public void testRefinedBow() {
		List<State> parts = List.of(BOW_LIMB_REFINED, STRING);
		Assertions.assertEquals("oak-bow", compositor.compositeName(parts));
	}
}

