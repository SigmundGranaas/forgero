package com.sigmundgranaas.forgero.common.tag.engine;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraphBuilder;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TagGraphBuilderTest {

	@Test
	void buildSucceedsForValidAcyclicGraph() {
		TagGraphBuilder builder = new TagGraphBuilder();
		builder.add(OpenIdentifier.of("test", "a"), Set.of(OpenIdentifier.of("test", "b")));
		builder.add(OpenIdentifier.of("test", "b"), Set.of(OpenIdentifier.of("test", "c")));

		assertDoesNotThrow(builder::build);
	}

	@Test
	void builderThrowsExceptionOnDirectCycle() {
		TagGraphBuilder builder = new TagGraphBuilder();
		OpenIdentifier a = OpenIdentifier.of("cycle", "a");
		OpenIdentifier b = OpenIdentifier.of("cycle", "b");

		builder.add(a, Set.of(b));
		builder.add(b, Set.of(a));

		IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
		assertTrue(e.getMessage().contains("Cycle detected in tag hierarchy"));
	}

	@Test
	void builderThrowsExceptionOnTransitiveCycle() {
		TagGraphBuilder builder = new TagGraphBuilder();
		OpenIdentifier a = OpenIdentifier.of("cycle", "a");
		OpenIdentifier b = OpenIdentifier.of("cycle", "b");
		OpenIdentifier c = OpenIdentifier.of("cycle", "c");

		builder.add(b, Set.of(a));
		builder.add(c, Set.of(b));
		builder.add(a, Set.of(c));

		IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
		assertTrue(e.getMessage().contains("Cycle detected in tag hierarchy"));
	}
}
