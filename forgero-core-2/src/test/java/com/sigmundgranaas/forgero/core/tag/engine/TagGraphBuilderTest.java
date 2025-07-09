package com.sigmundgranaas.forgero.core.tag.engine;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraphBuilder;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TagGraphBuilderTest {

	@Test
	void buildSucceedsForValidAcyclicGraph() {
		TagGraphBuilder builder = new TagGraphBuilder();
		builder.add(new OpenIdentifier("test", "a"), Set.of(new OpenIdentifier("test", "b")));
		builder.add(new OpenIdentifier("test", "b"), Set.of(new OpenIdentifier("test", "c")));

		assertDoesNotThrow(builder::build);
	}

	@Test
	void builderThrowsExceptionOnDirectCycle() {
		TagGraphBuilder builder = new TagGraphBuilder();
		OpenIdentifier a = new OpenIdentifier("cycle", "a");
		OpenIdentifier b = new OpenIdentifier("cycle", "b");

		builder.add(a, Set.of(b));
		builder.add(b, Set.of(a));

		IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
		assertTrue(e.getMessage().contains("Cycle detected in tag hierarchy"));
	}

	@Test
	void builderThrowsExceptionOnTransitiveCycle() {
		TagGraphBuilder builder = new TagGraphBuilder();
		OpenIdentifier a = new OpenIdentifier("cycle", "a");
		OpenIdentifier b = new OpenIdentifier("cycle", "b");
		OpenIdentifier c = new OpenIdentifier("cycle", "c");

		builder.add(b, Set.of(a));
		builder.add(c, Set.of(b));
		builder.add(a, Set.of(c));

		IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
		assertTrue(e.getMessage().contains("Cycle detected in tag hierarchy"));
	}
}
