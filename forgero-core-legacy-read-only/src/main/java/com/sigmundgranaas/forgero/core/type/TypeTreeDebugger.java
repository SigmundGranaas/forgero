package com.sigmundgranaas.forgero.core.type;

import com.sigmundgranaas.forgero.core.resource.data.v2.data.TypeData;

import java.util.List;

public class TypeTreeDebugger {
	private final TypeTree tree;
	private static final String LINE_SEPARATOR = System.lineSeparator();
	private static final String SECTION_SEPARATOR = LINE_SEPARATOR + "=" + "=".repeat(50) + LINE_SEPARATOR;

	public TypeTreeDebugger(TypeTree tree) {
		this.tree = tree;
	}

	public String debug() {
		StringBuilder sb = new StringBuilder();
		sb.append("Type Tree Debug Output").append(SECTION_SEPARATOR);

		// Process each root node independently
		List<MutableTypeNode> rootNodes = tree.nodes().stream()
				.filter(node -> node.parent().isEmpty())
				.toList();

		for (MutableTypeNode rootNode : rootNodes) {
			sb.append("Root Type: ").append(rootNode.name())
					.append(LINE_SEPARATOR)
					.append(debugRootNode(rootNode))
					.append(SECTION_SEPARATOR);
		}

		// Add missing nodes section if any exist
		appendMissingNodesSection(sb);

		return sb.toString();
	}

	private String debugRootNode(MutableTypeNode rootNode) {
		StringBuilder sb = new StringBuilder();
		sb.append("{").append(LINE_SEPARATOR);
		appendNodeDetails(rootNode, sb, 2);
		sb.append("}");
		return sb.toString();
	}

	private void appendNodeDetails(MutableTypeNode node, StringBuilder sb, int indent) {
		String indentStr = " ".repeat(indent);

		// Node name
		sb.append(indentStr).append("\"name\": \"").append(node.name()).append("\",")
				.append(LINE_SEPARATOR);

		// Type information
		Type nodeType = node.type();
		sb.append(indentStr).append("\"type\": {")
				.append(LINE_SEPARATOR)
				.append(indentStr).append("  \"typeName\": \"").append(nodeType.typeName()).append("\"")
				.append(LINE_SEPARATOR)
				.append(indentStr).append("},")
				.append(LINE_SEPARATOR);

		// Resource information
		appendResourceInfo(node, sb, indent);

		// Parent information
		appendParentInfo(node, sb, indent);

		// Children
		appendChildrenInfo(node, sb, indent);
	}

	private void appendResourceInfo(MutableTypeNode node, StringBuilder sb, int indent) {
		String indentStr = " ".repeat(indent);
		sb.append(indentStr).append("\"resources\": {")
				.append(LINE_SEPARATOR);

		// Get all resource types from the node's resourceMap
		// Note: We'll need to add a method to MutableTypeNode to expose resource types if needed

		sb.append(indentStr).append("},")
				.append(LINE_SEPARATOR);
	}

	private void appendParentInfo(MutableTypeNode node, StringBuilder sb, int indent) {
		String indentStr = " ".repeat(indent);
		List<MutableTypeNode> parents = node.parent();

		if (!parents.isEmpty()) {
			sb.append(indentStr).append("\"parents\": [")
					.append(LINE_SEPARATOR);

			for (int i = 0; i < parents.size(); i++) {
				sb.append(indentStr).append("  \"").append(parents.get(i).name()).append("\"");
				if (i < parents.size() - 1) {
					sb.append(",");
				}
				sb.append(LINE_SEPARATOR);
			}

			sb.append(indentStr).append("],")
					.append(LINE_SEPARATOR);
		}
	}

	private void appendChildrenInfo(MutableTypeNode node, StringBuilder sb, int indent) {
		String indentStr = " ".repeat(indent);
		List<MutableTypeNode> children = node.children();

		if (!children.isEmpty()) {
			sb.append(indentStr).append("\"children\": [")
					.append(LINE_SEPARATOR);

			for (int i = 0; i < children.size(); i++) {
				sb.append(indentStr).append("  {")
						.append(LINE_SEPARATOR);
				appendNodeDetails(children.get(i), sb, indent + 4);
				sb.append(indentStr).append("  }");

				if (i < children.size() - 1) {
					sb.append(",");
				}
				sb.append(LINE_SEPARATOR);
			}

			sb.append(indentStr).append("]")
					.append(LINE_SEPARATOR);
		}
	}

	private void appendMissingNodesSection(StringBuilder sb) {
		List<TypeData> missingNodes = tree.getMissingNodes();

		if (missingNodes != null && !missingNodes.isEmpty()) {
			sb.append("Missing Nodes").append(LINE_SEPARATOR)
					.append("{").append(LINE_SEPARATOR);

			for (int i = 0; i < missingNodes.size(); i++) {
				TypeData missingNode = missingNodes.get(i);
				sb.append("  \"").append(missingNode.name()).append("\": {")
						.append(LINE_SEPARATOR)
						.append("    \"name\": \"").append(missingNode.name()).append("\",")
						.append(LINE_SEPARATOR)
						.append("    \"parent\": ")
						.append(missingNode.parent().map(p -> "\"" + p + "\"").orElse("null"))
						.append(LINE_SEPARATOR)
						.append("  }");

				if (i < missingNodes.size() - 1) {
					sb.append(",");
				}
				sb.append(LINE_SEPARATOR);
			}

			sb.append("}").append(LINE_SEPARATOR);
		}
	}
}
