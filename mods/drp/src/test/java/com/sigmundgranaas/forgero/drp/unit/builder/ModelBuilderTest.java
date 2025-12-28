package com.sigmundgranaas.forgero.drp.unit.builder;

import com.sigmundgranaas.forgero.drp.api.model.ModelBuilder;
import com.sigmundgranaas.forgero.drp.api.model.ModelOverrideBuilder;
import com.sigmundgranaas.forgero.drp.api.model.TexturesBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ModelBuilder implementations.
 */
class ModelBuilderTest {

	@Test
	void testModelBuilderWithParent() {
		ModelBuilder builder = ModelBuilder.create()
				.parent("item/generated");

		assertEquals("item/generated", builder.getParent());
	}

	@Test
	void testModelBuilderWithTextures() {
		ModelBuilder builder = ModelBuilder.create()
				.parent("item/generated")
				.textures(tex -> tex
						.layer0("forgero:item/iron_blade")
						.layer1("forgero:item/iron_binding"));

		assertNotNull(builder.getTextures());
		assertEquals("forgero:item/iron_blade", builder.getTextures().getTextures().get("layer0"));
		assertEquals("forgero:item/iron_binding", builder.getTextures().getTextures().get("layer1"));
	}

	@Test
	void testTexturesBuilder() {
		TexturesBuilder builder = TexturesBuilder.create()
				.layer0("namespace:path0")
				.layer1("namespace:path1")
				.layer2("namespace:path2")
				.particle("namespace:particle");

		assertEquals("namespace:path0", builder.getTextures().get("layer0"));
		assertEquals("namespace:path1", builder.getTextures().get("layer1"));
		assertEquals("namespace:path2", builder.getTextures().get("layer2"));
		assertEquals("namespace:particle", builder.getTextures().get("particle"));
	}

	@Test
	void testTexturesBuilderWithLayer() {
		TexturesBuilder builder = TexturesBuilder.create()
				.layer(0, "namespace:tex0")
				.layer(5, "namespace:tex5");

		assertEquals("namespace:tex0", builder.getTextures().get("layer0"));
		assertEquals("namespace:tex5", builder.getTextures().get("layer5"));
	}

	@Test
	void testTexturesBuilderVariable() {
		TexturesBuilder builder = TexturesBuilder.create()
				.variable("custom_texture", "namespace:custom");

		assertEquals("namespace:custom", builder.getTextures().get("custom_texture"));
	}

	@Test
	void testModelOverrideBuilder() {
		ModelOverrideBuilder builder = ModelOverrideBuilder.create()
				.customModelData(42)
				.model("forgero:item/custom_model");

		assertEquals(42f, builder.getPredicates().get("custom_model_data"));
		assertEquals("forgero:item/custom_model", builder.getModel());
	}

	@Test
	void testModelOverrideBuilderDamage() {
		ModelOverrideBuilder builder = ModelOverrideBuilder.create()
				.damage(0.5f)
				.damaged(true)
				.model("item/damaged");

		assertEquals(0.5f, builder.getPredicates().get("damage"));
		assertEquals(1f, builder.getPredicates().get("damaged"));
	}

	@Test
	void testModelOverrideBuilderBow() {
		ModelOverrideBuilder builder = ModelOverrideBuilder.create()
				.pulling(1f)
				.pull(0.65f)
				.model("item/bow_pulling");

		assertEquals(1f, builder.getPredicates().get("pulling"));
		assertEquals(0.65f, builder.getPredicates().get("pull"));
	}

	@Test
	void testModelOverrideBuilderCustomPredicate() {
		ModelOverrideBuilder builder = ModelOverrideBuilder.create()
				.predicate("custom:predicate", 0.75f)
				.model("item/custom");

		assertEquals(0.75f, builder.getPredicates().get("custom:predicate"));
	}

	@Test
	void testModelBuilderWithOverrides() {
		ModelBuilder builder = ModelBuilder.create()
				.parent("item/bow")
				.addOverride(override -> override
						.predicate("pulling", 1f)
						.model("item/bow_pulling_0"))
				.addOverride(override -> override
						.predicate("pulling", 1f)
						.predicate("pull", 0.65f)
						.model("item/bow_pulling_1"));

		assertEquals(2, builder.getOverrides().size());
	}

	@Test
	void testModelBuilderWithDisplay() {
		ModelBuilder builder = ModelBuilder.create()
				.parent("item/generated")
				.display("gui",
						new float[]{0, 0, 0},
						new float[]{0, 0, 0},
						new float[]{1, 1, 1})
				.display("thirdperson_righthand",
						new float[]{45, 0, 0},
						new float[]{0, 2, 0},
						new float[]{0.5f, 0.5f, 0.5f});

		assertEquals(2, builder.getDisplay().size());
		assertNotNull(builder.getDisplay().get("gui"));
		assertNotNull(builder.getDisplay().get("thirdperson_righthand"));
	}

	@Test
	void testModelBuilderSetTexturesDirectly() {
		TexturesBuilder textures = TexturesBuilder.create()
				.layer0("namespace:texture");

		ModelBuilder builder = ModelBuilder.create()
				.parent("item/generated")
				.textures(textures);

		assertSame(textures, builder.getTextures());
	}
}
