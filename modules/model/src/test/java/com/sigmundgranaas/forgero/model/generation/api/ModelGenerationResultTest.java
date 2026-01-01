package com.sigmundgranaas.forgero.model.generation.api;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ArmorModelDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelDTO;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ModelGenerationResult - container for generation results.
 */
class ModelGenerationResultTest {

	@Test
	void testCreate_emptyResult() {
		ModelGenerationResult result = new ModelGenerationResult(
				Collections.emptyMap(),
				Collections.emptyMap(),
				Collections.emptyList()
		);

		assertNotNull(result);
		assertTrue(result.generatedModels().isEmpty());
		assertTrue(result.generatedArmorModels().isEmpty());
		assertTrue(result.textureGenerationTasks().isEmpty());
	}

	@Test
	void testCreate_withModels() {
		Map<OpenIdentifier, ModelDTO> models = new HashMap<>();
		ModelDTO model = new ModelDTO(
				"test",
				"composite",
				null, null, null, null, null, null, null, null, null
		);
		models.put(OpenIdentifier.of("forgero", "test"), model);

		ModelGenerationResult result = new ModelGenerationResult(
				models,
				Collections.emptyMap(),
				Collections.emptyList()
		);

		assertEquals(1, result.generatedModels().size());
		assertTrue(result.generatedModels().containsKey(OpenIdentifier.of("forgero", "test")));
		assertTrue(result.generatedArmorModels().isEmpty());
		assertTrue(result.textureGenerationTasks().isEmpty());
	}

	@Test
	void testCreate_withTextureTasks() {
		List<TextureGenerationTask> tasks = new ArrayList<>();
		tasks.add(new TextureGenerationTask("template", "palette", "output"));

		ModelGenerationResult result = new ModelGenerationResult(
				Collections.emptyMap(),
				Collections.emptyMap(),
				tasks
		);

		assertEquals(1, result.textureGenerationTasks().size());
		assertEquals("template", result.textureGenerationTasks().get(0).template());
	}

	@Test
	void testCreate_fullResult() {
		Map<OpenIdentifier, ModelDTO> itemModels = Map.of(
				OpenIdentifier.of("forgero", "item1"),
				new ModelDTO("item1", "composite", null, null, null, null, null, null, null, null, null)
		);

		Map<OpenIdentifier, ArmorModelDTO> armorModels = Map.of(
				OpenIdentifier.of("forgero", "armor1"),
				new ArmorModelDTO(OpenIdentifier.of("forgero", "armor1"), null, null, null, null, null, null)
		);

		List<TextureGenerationTask> tasks = List.of(
				new TextureGenerationTask("template1", "palette1", "output1"),
				new TextureGenerationTask("template2", "palette2", "output2")
		);

		ModelGenerationResult result = new ModelGenerationResult(
				itemModels,
				armorModels,
				tasks
		);

		assertEquals(1, result.generatedModels().size());
		assertEquals(1, result.generatedArmorModels().size());
		assertEquals(2, result.textureGenerationTasks().size());
	}

	@Test
	void testEquals_sameContent_areEqual() {
		Map<OpenIdentifier, ModelDTO> models = Map.of(
				OpenIdentifier.of("forgero", "test"),
				new ModelDTO("test", "composite", null, null, null, null, null, null, null, null, null)
		);

		ModelGenerationResult result1 = new ModelGenerationResult(
				models,
				Collections.emptyMap(),
				Collections.emptyList()
		);

		ModelGenerationResult result2 = new ModelGenerationResult(
				models,
				Collections.emptyMap(),
				Collections.emptyList()
		);

		assertEquals(result1, result2);
	}

	@Test
	void testCreate_nullMaps_accepted() {
		// Records allow null values without validation
		ModelGenerationResult result = new ModelGenerationResult(null, null, null);

		assertNotNull(result);
		assertNull(result.generatedModels());
		assertNull(result.generatedArmorModels());
		assertNull(result.textureGenerationTasks());
	}

	@Test
	void testCreate_multipleModels() {
		Map<OpenIdentifier, ModelDTO> models = new HashMap<>();
		models.put(OpenIdentifier.of("forgero", "model1"),
				new ModelDTO("model1", "composite", null, null, null, null, null, null, null, null, null));
		models.put(OpenIdentifier.of("forgero", "model2"),
				new ModelDTO("model2", "texture", null, null, null, null, null, null, null, null, null));
		models.put(OpenIdentifier.of("forgero", "model3"),
				new ModelDTO("model3", "composite", null, null, null, null, null, null, null, null, null));

		ModelGenerationResult result = new ModelGenerationResult(
				models,
				Collections.emptyMap(),
				Collections.emptyList()
		);

		assertEquals(3, result.generatedModels().size());
		assertTrue(result.generatedModels().containsKey(OpenIdentifier.of("forgero", "model1")));
		assertTrue(result.generatedModels().containsKey(OpenIdentifier.of("forgero", "model2")));
		assertTrue(result.generatedModels().containsKey(OpenIdentifier.of("forgero", "model3")));
	}

	@Test
	void testCreate_multipleTextureTasks() {
		List<TextureGenerationTask> tasks = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			tasks.add(new TextureGenerationTask(
					"template" + i,
					"palette" + i,
					"output" + i
			));
		}

		ModelGenerationResult result = new ModelGenerationResult(
				Collections.emptyMap(),
				Collections.emptyMap(),
				tasks
		);

		assertEquals(10, result.textureGenerationTasks().size());
	}

	@Test
	void testToString_containsFieldNames() {
		ModelGenerationResult result = new ModelGenerationResult(
				Collections.emptyMap(),
				Collections.emptyMap(),
				Collections.emptyList()
		);

		String toString = result.toString();

		assertTrue(toString.contains("generatedModels") ||
						toString.contains("ModelGenerationResult"),
				"toString should contain class or field information");
	}
}
