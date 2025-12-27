package com.sigmundgranaas.forgero.drp.unit.builder;

import com.sigmundgranaas.forgero.drp.api.recipe.IngredientBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapedRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.ShapelessRecipeBuilder;
import com.sigmundgranaas.forgero.drp.api.recipe.SmithingRecipeBuilder;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RecipeBuilder implementations.
 */
class RecipeBuilderTest {

	// ============================================================
	// Shapeless Recipe Tests
	// ============================================================

	@Test
	void testShapelessRecipeBasic() {
		ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.create()
				.addIngredient("minecraft:iron_ingot")
				.addIngredient("minecraft:stick")
				.result("minecraft:iron_sword");

		assertEquals(2, builder.getIngredients().size());
		assertEquals("minecraft", builder.getResult().getNamespace());
		assertEquals("iron_sword", builder.getResult().getPath());
		assertEquals(1, builder.getResultCount());
	}

	@Test
	void testShapelessRecipeWithCount() {
		ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.create()
				.addIngredient("minecraft:dirt")
				.result("minecraft:gravel", 4);

		assertEquals(4, builder.getResultCount());
	}

	@Test
	void testShapelessRecipeWithTagIngredient() {
		ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.create()
				.addTagIngredient("minecraft:planks")
				.result("minecraft:stick");

		assertEquals(1, builder.getIngredients().size());
		var entries = builder.getIngredients().get(0).getEntries();
		assertEquals(1, entries.size());
		assertTrue(entries.get(0).isTag());
	}

	@Test
	void testShapelessRecipeMultipleOfSameIngredient() {
		ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.create()
				.addIngredient("minecraft:diamond", 3)
				.result("minecraft:diamond_block");

		assertEquals(3, builder.getIngredients().size());
	}

	@Test
	void testShapelessRecipeWithGroup() {
		ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.create()
				.group("tools")
				.addIngredient("minecraft:stick")
				.result("minecraft:item");

		assertEquals("tools", builder.getGroup());
	}

	@Test
	void testShapelessRecipeWithCustomIngredient() {
		ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.create()
				.addIngredient(ing -> ing
						.item("minecraft:diamond")
						.or()
						.item("minecraft:emerald"))
				.result("minecraft:gem");

		assertEquals(1, builder.getIngredients().size());
		assertEquals(2, builder.getIngredients().get(0).getEntries().size());
	}

	// ============================================================
	// Shaped Recipe Tests
	// ============================================================

	@Test
	void testShapedRecipeBasic() {
		ShapedRecipeBuilder builder = ShapedRecipeBuilder.create()
				.pattern("III")
				.pattern(" S ")
				.pattern(" S ")
				.key('I', "minecraft:iron_ingot")
				.key('S', "minecraft:stick")
				.result("minecraft:iron_pickaxe");

		assertEquals(3, builder.getPattern().size());
		assertEquals("III", builder.getPattern().get(0));
		assertEquals(" S ", builder.getPattern().get(1));
		assertEquals(" S ", builder.getPattern().get(2));
		assertEquals(2, builder.getKeys().size());
	}

	@Test
	void testShapedRecipeWithTagKey() {
		ShapedRecipeBuilder builder = ShapedRecipeBuilder.create()
				.pattern("PPP")
				.keyTag('P', "minecraft:planks")
				.result("minecraft:crafting_table");

		assertTrue(builder.getKeys().containsKey('P'));
		var entries = builder.getKeys().get('P').getEntries();
		assertTrue(entries.get(0).isTag());
	}

	@Test
	void testShapedRecipeTooManyRows() {
		ShapedRecipeBuilder builder = ShapedRecipeBuilder.create()
				.pattern("AAA")
				.pattern("BBB")
				.pattern("CCC");

		assertThrows(IllegalArgumentException.class, () ->
				builder.pattern("DDD"));
	}

	@Test
	void testShapedRecipeRowTooLong() {
		ShapedRecipeBuilder builder = ShapedRecipeBuilder.create();

		assertThrows(IllegalArgumentException.class, () ->
				builder.pattern("AAAA"));
	}

	@Test
	void testShapedRecipeWithCustomKeyIngredient() {
		ShapedRecipeBuilder builder = ShapedRecipeBuilder.create()
				.pattern("X")
				.key('X', ing -> ing
						.item("minecraft:diamond")
						.or()
						.tag("forge:gems"))
				.result("minecraft:gem");

		assertEquals(2, builder.getKeys().get('X').getEntries().size());
	}

	// ============================================================
	// Smithing Recipe Tests
	// ============================================================

	@Test
	void testSmithingRecipeBasic() {
		SmithingRecipeBuilder builder = SmithingRecipeBuilder.create()
				.template("minecraft:netherite_upgrade_smithing_template")
				.base("minecraft:diamond_sword")
				.addition("minecraft:netherite_ingot")
				.result("minecraft:netherite_sword");

		assertNotNull(builder.getTemplate());
		assertNotNull(builder.getBase());
		assertNotNull(builder.getAddition());
		assertEquals("minecraft", builder.getResult().getNamespace());
		assertEquals("netherite_sword", builder.getResult().getPath());
	}

	@Test
	void testSmithingRecipeWithTags() {
		SmithingRecipeBuilder builder = SmithingRecipeBuilder.create()
				.templateTag("forge:upgrade_templates")
				.baseTag("forge:tools/swords")
				.additionTag("forge:ingots/netherite")
				.result("minecraft:netherite_sword");

		assertTrue(builder.getTemplate().getEntries().get(0).isTag());
		assertTrue(builder.getBase().getEntries().get(0).isTag());
		assertTrue(builder.getAddition().getEntries().get(0).isTag());
	}

	// ============================================================
	// Ingredient Builder Tests
	// ============================================================

	@Test
	void testIngredientBuilderItem() {
		IngredientBuilder builder = IngredientBuilder.create()
				.item("minecraft:diamond");

		assertEquals(1, builder.getEntries().size());
		assertFalse(builder.getEntries().get(0).isTag());
	}

	@Test
	void testIngredientBuilderTag() {
		IngredientBuilder builder = IngredientBuilder.create()
				.tag("minecraft:planks");

		assertEquals(1, builder.getEntries().size());
		assertTrue(builder.getEntries().get(0).isTag());
	}

	@Test
	void testIngredientBuilderMultiple() {
		IngredientBuilder builder = IngredientBuilder.create()
				.item("minecraft:diamond")
				.or()
				.item("minecraft:emerald")
				.or()
				.tag("forge:gems");

		assertEquals(3, builder.getEntries().size());
	}

	@Test
	void testIngredientBuilderTagWithHash() {
		IngredientBuilder builder = IngredientBuilder.create()
				.tag("#minecraft:planks");

		assertEquals("planks", builder.getEntries().get(0).id().getPath());
	}
}
