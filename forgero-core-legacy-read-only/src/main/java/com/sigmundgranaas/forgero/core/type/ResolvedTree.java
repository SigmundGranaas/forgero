package com.sigmundgranaas.forgero.core.type;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Optional;

public class ResolvedTree implements ResolvedTypeTree {
	private final ImmutableList<TypeNode> rootNodes;

	public ResolvedTree(List<TypeNode> rootNodes) {
		this.rootNodes = ImmutableList.<TypeNode>builder().addAll(rootNodes).build();
	}

	public Optional<TypeNode> find(String name) {
		return rootNodes.stream().map(typeNode -> find(name, typeNode)).flatMap(Optional::stream).findAny();
	}

	public Optional<TypeNode> find(String name, TypeNode node) {
		if (node.name().equals(name)) {
			return Optional.of(node);
		}
		return node.children().stream().map(newNode -> find(name, newNode)).flatMap(Optional::stream).findAny();
	}
}
