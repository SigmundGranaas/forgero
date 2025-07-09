package com.sigmundgranaas.forgero.common.tags.engine;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.Comparator;

public class DotGraphRenderer {

	/**
	 * Renders a TagGraph into a string in the DOT graph description language.
	 *
	 * @param graph The TagGraph to render.
	 * @param graphName The name for the graph.
	 * @return A string containing the DOT representation of the graph.
	 */
	public String render(TagGraph graph, String graphName) {
		StringBuilder dotBuilder = new StringBuilder();
		dotBuilder.append("digraph ").append(graphName).append(" {\n");
		dotBuilder.append("    rankdir=LR;\n");
		dotBuilder.append("    node [shape=box, style=rounded];\n\n");

		graph.getAllIdentifiers().stream()
				.sorted(Comparator.comparing(OpenIdentifier::toString))
				.forEach(childId -> {
					graph.getParents(childId).stream()
							.sorted(Comparator.comparing(OpenIdentifier::toString))
							.forEach(parentId -> {
								dotBuilder.append("    \"")
										.append(childId.toString())
										.append("\" -> \"")
										.append(parentId.toString())
										.append("\";\n");
							});
				});

		dotBuilder.append("}");
		return dotBuilder.toString();
	}
}
