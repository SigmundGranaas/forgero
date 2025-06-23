package com.sigmundgranaas.forgero.tag.engine;

import com.sigmundgranaas.forgero.core.identifier.OpenIdentifier;
import com.sigmundgranaas.forgero.core.tags.core.engine.DotGraphRenderer;
import com.sigmundgranaas.forgero.core.tags.core.engine.TagGraph;
import com.sigmundgranaas.forgero.core.tags.core.engine.TagGraphBuilder;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DotGraphRendererTest {

	@Test
	void rendererCreatesCorrectRepresentation() {
		TagGraphBuilder builder = new TagGraphBuilder();
		OpenIdentifier part = new OpenIdentifier("forgero", "part");
		OpenIdentifier weaponHead = new OpenIdentifier("forgero", "weapon_head");
		OpenIdentifier swordBlade = new OpenIdentifier("forgero", "sword_blade");
		OpenIdentifier magical = new OpenIdentifier("forgero", "magical");

		builder.add(weaponHead, Set.of(part));
		builder.add(swordBlade, Set.of(weaponHead, magical));
		TagGraph graph = builder.build();

		DotGraphRenderer renderer = new DotGraphRenderer();

		String dotOutput = renderer.render(graph, "TestGraph");

		assertTrue(dotOutput.contains("\"forgero:sword_blade\" -> \"forgero:magical\""));
		assertTrue(dotOutput.contains("\"forgero:sword_blade\" -> \"forgero:weapon_head\""));
		assertTrue(dotOutput.contains("\"forgero:weapon_head\" -> \"forgero:part\""));
		assertTrue(dotOutput.contains("digraph TestGraph {"));
		assertTrue(dotOutput.endsWith("}"));
	}
}
