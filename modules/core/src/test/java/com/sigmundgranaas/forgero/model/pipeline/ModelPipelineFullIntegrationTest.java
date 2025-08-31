package com.sigmundgranaas.forgero.model.pipeline;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.api.structure.StructureSlot;
import com.sigmundgranaas.forgero.core.component.impl.StructuredEquipment;
import com.sigmundgranaas.forgero.core.component.impl.StructuredPart;
import com.sigmundgranaas.forgero.model.generation.api.ModelGenerationResult;
import com.sigmundgranaas.forgero.model.loading.impl.dto.ModelDTO;
import com.sigmundgranaas.forgero.model.pipeline.api.ModelDataInitializer;
import com.sigmundgranaas.forgero.model.pipeline.api.ModelInitializationResult;
import com.sigmundgranaas.forgero.model.registry.api.armor.ArmorModelRegistry;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.model.registry.impl.MapBackedArmorModelRegistry;
import com.sigmundgranaas.forgero.model.registry.impl.MapBackedModelRegistry;
import com.sigmundgranaas.forgero.model.texture.api.TextureGenerator;
import com.sigmundgranaas.forgero.model.texture.impl.AwtPalettizedTextureGenerator;
import com.sigmundgranaas.forgero.model.texture.impl.DefaultTextureGenerator;
import com.sigmundgranaas.forgero.model.texture.impl.FileTextureWriter;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelPipelineFullIntegrationTest {

	private static final String OUTPUT_DIRECTORY = "build/test_results/model_pipeline_output";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private Map<OpenIdentifier, Component> components;
	private TagGraph tagGraph;


	@BeforeEach
	void setUp() {
		// Define Tags
		var partTag = new OpenIdentifier("forgero:part");
		var toolTag = new OpenIdentifier("forgero:tool");
		var metalTag = new OpenIdentifier("forgero:metal");
		var woodTag = new OpenIdentifier("forgero:wood");
		var swordTag = new OpenIdentifier("forgero:sword");
		var swordBladePartTag = new OpenIdentifier("forgero:sword_blade_part");
		var handlePartTag = new OpenIdentifier("forgero:handle_part");


		// Define IDs, including sub-paths for correct file output
		var ironId = new OpenIdentifier("forgero:iron");
		var oakId = new OpenIdentifier("forgero:oak");
		var ironBladeId = new OpenIdentifier("forgero:parts/iron-sword_blade");
		var oakHandleId = new OpenIdentifier("forgero:parts/oak-handle");
		var ironSwordId = new OpenIdentifier("forgero:equipment/iron-sword");

		// Setup TagGraph
		tagGraph = new TagGraph(Map.of(
				ironId, Set.of(metalTag, partTag),
				oakId, Set.of(woodTag, partTag),
				ironBladeId, Set.of(partTag, swordBladePartTag),
				oakHandleId, Set.of(partTag, handlePartTag),
				ironSwordId, Set.of(toolTag, swordTag)
		));

		// Create mock runtime components, including composites with material children
		Component iron = new StructuredPart(ironId, Set.of(metalTag, partTag), new HashMap<>(), new ComponentStructure(Collections.emptyMap()));
		Component oak = new StructuredPart(oakId, Set.of(woodTag, partTag), new HashMap<>(), new ComponentStructure(Collections.emptyMap()));

		Component ironBlade = new StructuredPart(
				ironBladeId,
				Set.of(partTag, swordBladePartTag),
				new HashMap<>(),
				new ComponentStructure(Map.of(new OpenIdentifier("forgero:material"), new StructureSlot(new OpenIdentifier("forgero:material"), iron.id(), "", iron)))
		);

		Component oakHandle = new StructuredPart(
				oakHandleId,
				Set.of(partTag, handlePartTag),
				new HashMap<>(),
				new ComponentStructure(Map.of(new OpenIdentifier("forgero:material"), new StructureSlot(new OpenIdentifier("forgero:material"), oak.id(), "", oak)))
		);

		Component ironSword = new StructuredEquipment(
				ironSwordId,
				Set.of(toolTag, swordTag),
				new HashMap<>(),
				new ComponentStructure(Map.of(
						new OpenIdentifier("forgero:blade"), new StructureSlot(new OpenIdentifier("forgero:blade"), ironBlade.id(), "", ironBlade),
						new OpenIdentifier("forgero:handle"), new StructureSlot(new OpenIdentifier("forgero:handle"), oakHandle.id(), "", oakHandle)
				))
		);

		List<Component> componentList = List.of(iron, oak, ironBlade, oakHandle, ironSword);
		components = componentList.stream().collect(Collectors.toMap(Component::id, Function.identity()));
	}

	@Test
	void testFullPipelineGeneratesFilesCorrectly() {
		ResourceProvider resourceProvider = new ClassPathResourceProvider("/assets");
		ItemModelRegistry modelRegistry = new MapBackedModelRegistry();
		ArmorModelRegistry armorModelRegistry = new MapBackedArmorModelRegistry();

		// Initialize models without writing files
		ModelDataInitializer modelInitializer = new ModelDataInitializer(resourceProvider, "forgero");
		ModelInitializationResult initResult = modelInitializer.initialize(components, tagGraph, modelRegistry, armorModelRegistry);

		// Manually write the generated assets to a specific directory for inspection
		writeGeneratedAssets(initResult.generationResult(), resourceProvider);

		// Assertions
		ModelGenerationResult result = initResult.generationResult();
		assertEquals(4, result.generatedModels().size());

		File bladeTexture = new File(OUTPUT_DIRECTORY, "assets/forgero/item/iron-sword_blade.png");
		assertTrue(bladeTexture.exists() && bladeTexture.length() > 0, "Texture for iron-sword_blade should be generated.");

		File handleTexture = new File(OUTPUT_DIRECTORY, "assets/forgero/item/oak-handle.png");
		assertTrue(handleTexture.exists() && handleTexture.length() > 0, "Texture for oak-handle should be generated.");

		File guardTexture = new File(OUTPUT_DIRECTORY, "assets/forgero/item/iron-sword_guard.png");
		assertTrue(guardTexture.exists() && guardTexture.length() > 0, "Texture for contextual iron guard should be generated.");

		File bladeModelFile = new File(OUTPUT_DIRECTORY, "assets/forgero/models/iron-sword_blade.json");
		assertTrue(bladeModelFile.exists() && bladeModelFile.length() > 0, "Model JSON for iron-sword_blade should be written.");

		File handleModelFile = new File(OUTPUT_DIRECTORY, "assets/forgero/models/oak-handle.json");
		assertTrue(handleModelFile.exists() && handleModelFile.length() > 0, "Model JSON for oak-handle should be written.");

		File swordModelFile = new File(OUTPUT_DIRECTORY, "assets/forgero/models/equipment/iron-sword.json");
		assertTrue(swordModelFile.exists() && swordModelFile.length() > 0, "Model JSON for iron-sword should be written.");

		System.out.println("Generated files written to: " + new File(OUTPUT_DIRECTORY).getAbsolutePath());
	}

	private void writeGeneratedAssets(ModelGenerationResult result, ResourceProvider resourceProvider) {
		TextureGenerator textureGenerator = new DefaultTextureGenerator(
				resourceProvider,
				new AwtPalettizedTextureGenerator(),
				new FileTextureWriter(ModelPipelineFullIntegrationTest.OUTPUT_DIRECTORY)
		);
		textureGenerator.generate(result.textureGenerationTasks());

		result.generatedModels().forEach(this::writeModelToFile);
	}

	private void writeModelToFile(OpenIdentifier id, ModelDTO dto) {
		try {
			Path modelPath = Path.of(ModelPipelineFullIntegrationTest.OUTPUT_DIRECTORY, "assets", id.namespace(), "models", id.path() + ".json");
			File modelFile = modelPath.toFile();
			//noinspection ResultOfMethodCallIgnored
			modelFile.getParentFile().mkdirs();
			try (FileWriter writer = new FileWriter(modelFile)) {
				GSON.toJson(dto, writer);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to write model file for ID: " + id, e);
		}
	}
}
