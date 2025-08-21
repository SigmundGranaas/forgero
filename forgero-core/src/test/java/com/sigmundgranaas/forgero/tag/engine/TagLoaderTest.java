package com.sigmundgranaas.forgero.tag.engine;

import com.sigmundgranaas.forgero.core.tags.core.engine.TagGraph;
import com.sigmundgranaas.forgero.core.tags.core.engine.TagLoader;
import com.sigmundgranaas.forgero.core.tags.core.engine.TagParser;
import com.sigmundgranaas.forgero.core.tags.core.engine.TagSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TagLoaderTest {

	private final IdentifierFactory factory = new IdentifierFactory.Builder().build();
	private final TagParser parser = new TagParser();

	@Test
	void loadsAndBuildsCorrectGraph() {
		TagSource source = () -> Map.of(
				"forgero:sword_blade", """
                    { "parents": ["forgero:weapon_head", "forgero:blade"] }
                """,
				"forgero:weapon_head", """
                    { "parent": "forgero:part" }
                """,
				"forgero:blade", "{ }",
				"forgero:part", "{ }"
		);

		TagLoader loader = new TagLoader(factory, parser);
		TagGraph graph = loader.load(List.of(source));

		OpenIdentifier swordBladeId = factory.of("forgero:sword_blade");
		OpenIdentifier partId = factory.of("forgero:part");
		OpenIdentifier bladeId = factory.of("forgero:blade");

		assertTrue(graph.isTagged(() -> Set.of(swordBladeId), partId));
		assertTrue(graph.isTagged(() -> Set.of(swordBladeId), bladeId));
		assertFalse(graph.isTagged(() -> Set.of(bladeId), partId));
	}

	@Test
	void loaderPropagatesCycleDetectionException() {
		TagSource cyclicSource = () -> Map.of(
				"cycle:a", "{ \"parent\": \"cycle:b\" }",
				"cycle:b", "{ \"parent\": \"cycle:a\" }"
		);

		TagLoader loader = new TagLoader(factory, parser);

		assertThrows(IllegalStateException.class, () -> loader.load(List.of(cyclicSource)));
	}

	@Test
	void loaderRejectsInvalidIdentifier() {
		TagSource invalidSource = () -> Map.of(
				"forgero:InvalidName", "{}"
		);

		TagLoader loader = new TagLoader(factory, parser);

		assertThrows(IllegalArgumentException.class, () -> loader.load(List.of(invalidSource)));
	}
}
