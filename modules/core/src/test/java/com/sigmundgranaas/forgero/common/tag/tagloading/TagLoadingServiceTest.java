package com.sigmundgranaas.forgero.common.tag.tagloading;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.api.Taggable;
import com.sigmundgranaas.forgero.common.tags.engine.TagLoadingService;
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

		// The root path within 'data' from which to load tags.
		// The service will strip the "tags/" prefix from the generated OpenIdentifier.
		OpenIdentifier tagsRootPath = factory.of("tags"); // This is the root for finding tag *files*

		// Act: Load the resolver from the test resources
		TagResolver resolver = service.loadTags(tagsRootPath);

		// Assert: Test the loaded resolver for correctness using canonical identifiers (NO 'tags/' prefix).
		// These IDs now represent the "clean" tags stored in the resolver.
		var oakId = factory.of("oak");
		var woodId = factory.of("wood");
		var materialId = factory.of("material");
		var metalId = factory.of("metal");
		var flammableId = factory.of("flammable");
		var metalToolMaterialId = factory.of("metal-tool-material");

		// Create Taggable test objects (their internal tags must match the canonical IDs)
		Taggable oakItem = () -> Set.of(oakId);
		Taggable metalItem = () -> Set.of(metalId);

		// Assertions for hierarchy and multi-parent inheritance
		assertTrue(resolver.hasTag(oakItem, woodId), "Oak should be tagged as Wood");
		assertTrue(resolver.hasTag(oakItem, materialId), "Oak should be transitively tagged as Material");
		assertTrue(resolver.hasTag(oakItem, flammableId), "Oak should also be tagged as Flammable");

		assertTrue(resolver.hasTag(metalItem, metalToolMaterialId), "Metal should be tagged as Metal-Tool-Material");
		assertTrue(resolver.hasTag(metalItem, materialId), "Metal should be transitively tagged as Material");
		assertFalse(resolver.hasTag(metalItem, woodId), "Metal should not be tagged as Wood");
	}
}
