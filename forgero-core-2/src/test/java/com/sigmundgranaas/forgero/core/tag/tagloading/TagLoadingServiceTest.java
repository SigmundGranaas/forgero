package com.sigmundgranaas.forgero.core.tag.tagloading;

import com.sigmundgranaas.forgero.core.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.tags.api.Taggable;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TagLoadingServiceTest {

	@Test
	void loadsTagsAndCreatesCorrectGraph() {
		// Setup: The service is configured to load from the 'data' directory in the classpath
		var factory = new IdentifierFactory.Builder().defaultNamespace("test-tags").build();
		TagLoadingService service = new TagLoadingService(factory);

		// The root path within 'data' from which to load tags
		OpenIdentifier tagsRootPath = factory.of("tags");

		// Act: Load the graph from the test resources
		TagGraph graph = service.loadTags(tagsRootPath);

		// Assert: Test the loaded graph for correctness using canonical identifiers (no .json)
		var oakId = factory.of("tags/oak");
		var woodId = factory.of("tags/wood");
		var materialId = factory.of("tags/material");
		var metalId = factory.of("tags/metal");
		var flammableId = factory.of("tags/flammable");
		var metalToolMaterialId = factory.of("tags/metal-tool-material");

		// Create Taggable test objects
		Taggable oakItem = () -> Set.of(oakId);
		Taggable metalItem = () -> Set.of(metalId);

		// Assertions for hierarchy and multi-parent inheritance
		assertTrue(graph.isTagged(oakItem, woodId), "Oak should be tagged as Wood");
		assertTrue(graph.isTagged(oakItem, materialId), "Oak should be transitively tagged as Material");
		assertTrue(graph.isTagged(oakItem, flammableId), "Oak should also be tagged as Flammable");

		assertTrue(graph.isTagged(metalItem, metalToolMaterialId), "Metal should be tagged as Metal-Tool-Material");
		assertTrue(graph.isTagged(metalItem, materialId), "Metal should be transitively tagged as Material");
		assertFalse(graph.isTagged(metalItem, woodId), "Metal should not be tagged as Wood");
	}
}
