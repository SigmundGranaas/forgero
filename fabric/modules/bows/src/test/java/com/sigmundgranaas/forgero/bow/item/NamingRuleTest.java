package com.sigmundgranaas.forgero.bow.item;

import static com.sigmundgranaas.forgero.bow.item.NamingRules.*;
import static com.sigmundgranaas.forgero.core.property.CalculationOrder.BASE;
import static com.sigmundgranaas.forgero.core.property.NumericOperation.ADDITION;
import static com.sigmundgranaas.forgero.core.type.Type.SCHEMATIC;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.api.identity.DefaultRules;
import com.sigmundgranaas.forgero.core.api.identity.ModificationRuleRegistry;
import com.sigmundgranaas.forgero.core.api.identity.sorting.SortingRuleRegistry;
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
	private ModificationRuleRegistry modificationRuleRegistry;
	private SortingRuleRegistry sorting;
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
			.build();

	public static State ARROW_HEAD = Construct.builder()
			.addIngredient(IRON)
			.addIngredient(ARROW_HEAD_SCHEMATIC)
			.type(Type.ARROW_HEAD)
			.build();

	public static State ARROW_HEAD_MASTER = Construct.builder()
			.addIngredient(IRON)
			.addIngredient(MASTER_HEAD_SCHEMATIC)
			.type(Type.ARROW_HEAD)
			.build();

	public static State BOW_LIMB_REFINED = Construct.builder()
			.addIngredient(OAK)
			.addIngredient(REFINED_BOW_LIMB_SCHEMATIC)
			.type(Type.BOW_LIMB)
			.build();

	public static State BOW_LIMB = Construct.builder()
			.addIngredient(OAK)
			.addIngredient(BOW_LIMB_SCHEMATIC)
			.type(Type.BOW_LIMB)
			.build();


	@BeforeEach
	public void prepareRegistry() {
		modificationRuleRegistry = ModificationRuleRegistry.local();
		sorting = SortingRuleRegistry.local();
		compositor = new NameCompositor(modificationRuleRegistry, sorting);
	}

	@Test
	public void testArrow() {
		modificationRuleRegistry.registerRule("forgero:handle", DefaultRules.handle.build());
		modificationRuleRegistry.registerRule("forgero:arrow_head", arrowHead.build());
		modificationRuleRegistry.registerRule("forgero:feather", feather.build());

		List<State> arrow = List.of(ARROW_HEAD, HANDLE, FEATHER);

		Assertions.assertEquals("iron-arrow", compositor.compositeName(arrow));
	}

	@Test
	public void testMastercraftedArrow() {
		modificationRuleRegistry.registerRule("forgero:handle", DefaultRules.handle.build());
		modificationRuleRegistry.registerRule("forgero:arrow_head", arrowHead.build());
		modificationRuleRegistry.registerRule("forgero:feather", feather.build());

		List<State> arrow = List.of(ARROW_HEAD_MASTER, HANDLE, FEATHER);

		Assertions.assertEquals("iron-arrow", compositor.compositeName(arrow));
	}

	@Test
	public void testBow() {
		modificationRuleRegistry.registerRule("forgero:bow_limb", bowLimb.build());
		modificationRuleRegistry.registerRule("forgero:string", string.build());

		List<State> parts = List.of(BOW_LIMB, STRING);

		Assertions.assertEquals("oak-bow", compositor.compositeName(parts));
	}

	@Test
	public void testRefinedBow() {
		modificationRuleRegistry.registerRule("forgero:bow_limb", bowLimb.build());
		modificationRuleRegistry.registerRule("forgero:string", string.build());

		List<State> parts = List.of(BOW_LIMB_REFINED, STRING);

		Assertions.assertEquals("oak-bow", compositor.compositeName(parts));
	}
}

