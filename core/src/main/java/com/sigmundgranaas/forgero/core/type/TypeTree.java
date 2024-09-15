package com.sigmundgranaas.forgero.core.type;


import com.sigmundgranaas.forgero.core.resource.data.v2.data.TypeData;
import com.sigmundgranaas.forgero.core.util.Identifiers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TypeTree implements UnresolvedTypeTree, MutableTypeTree {
	private final List<MutableTypeNode> rootNodes;
	private List<TypeData> missingNodes;
	private final ConcurrentHashMap<String, Optional<MutableTypeNode>> nodeCache;
	private final ConcurrentHashMap<Type, Optional<MutableTypeNode>> typeCache;


	public TypeTree() {
		this.rootNodes = new ArrayList<>();
		this.missingNodes = new ArrayList<>();
		this.nodeCache = new ConcurrentHashMap<>();
		this.typeCache = new ConcurrentHashMap<>();
	}

	private void invalidateCache() {
		nodeCache.clear();
		typeCache.clear();
	}

	public Optional<MutableTypeNode> addNode(TypeData nodeData) {
		invalidateCache();

		Optional<MutableTypeNode> nodeOpt = find(nodeData.name());
		if (nodeOpt.isEmpty()) {
			if (nodeData.parent().isPresent()) {
				return addNodeWithParent(nodeData.name(), nodeData.parent().get());
			} else {
				MutableTypeNode node = new MutableTypeNode(new ArrayList<>(), nodeData.name(), Collections.emptyList());
				rootNodes.add(node);
				return Optional.of(node);
			}
		} else {
			if(nodeData.parent().isPresent()){
				var parentOpt = find(nodeData.parent().get());
				if(parentOpt.isPresent()){
					parentOpt.get().addChild(nodeOpt.get());
					return nodeOpt;
				}else{
					if (missingNodes.stream().noneMatch(node -> node.name().equals(nodeData.name()))) {
						missingNodes.add(new TypeData(nodeData.name(), nodeData.parent(), Collections.emptyList()));
					}
				}
			}
		}
		return Optional.empty();
	}


	public void addNodes(List<TypeData> nodes) {
		invalidateCache();

		nodes.forEach(this::addNode);
		this.resolve();
	}

	private Optional<MutableTypeNode> addNodeWithParent(String name, String parent) {
		invalidateCache();

		var parentOpt = find(parent);

		if (parentOpt.isPresent()) {
			MutableTypeNode node = new MutableTypeNode(new ArrayList<>(), name, Collections.emptyList());
			return Optional.of(parentOpt.get().addChild(node));
		} else {
			if (missingNodes.stream().noneMatch(node -> node.name().equals(name))) {
				missingNodes.add(new TypeData(name, Optional.of(parent), Collections.emptyList()));
			}
		}
		return Optional.empty();
	}

	public synchronized ResolvedTypeTree resolve() {
		invalidateCache();

		if (this.missingNodes.isEmpty()) {
			return new ResolvedTree(resolveNodes());
		}
		int missing = missingNodes.size();

		var missingCopy = new ArrayList<>(missingNodes);

		var addedNodes = missingCopy.stream()
				.map(this::addNode)
				.flatMap(Optional::stream)
				.toList();

		removeMissingNodes(addedNodes);
		if (missing > addedNodes.size()) {
			resolve();
		}
		return new ResolvedTree(resolveNodes());
	}

	public List<TypeNode> resolveNodes() {
		invalidateCache();
		return rootNodes.stream().map(MutableTypeNode::resolve).toList();
	}

	public List<MutableTypeNode> nodes() {
		var list = new ArrayList<>(rootNodes);
		rootNodes.forEach(node -> node.allChildren(list));
		return list;
	}

	private void removeMissingNodes(List<MutableTypeNode> nodes) {
		invalidateCache();
		this.missingNodes = new ArrayList<>(missingNodes.stream().filter(missingNode -> nodes.stream().noneMatch(node -> node.name().equals(missingNode.name()))).toList());
	}

	public Optional<MutableTypeNode> find(String name) {
		return nodeCache.computeIfAbsent(name, k ->
				rootNodes.stream()
						.map(typeNode -> find(name, typeNode).map(MutableTypeNode.class::cast))
						.flatMap(Optional::stream)
						.findAny()
		);
	}

	public Optional<MutableTypeNode> find(Type type) {
		return typeCache.computeIfAbsent(type, k ->
				rootNodes.stream()
						.map(typeNode -> find(type.typeName(), typeNode).map(MutableTypeNode.class::cast))
						.flatMap(Optional::stream)
						.findAny()
		);
	}

	public Optional<MutableTypeNode> find(String name, MutableTypeNode node) {
		if (node.name().equals(name)) {
			return Optional.of(node);
		}
		return node.children().stream()
				.map(newNode -> find(name, newNode))
				.flatMap(Optional::stream)
				.findAny();
	}

	public Type type(String type) {
		return find(type).map(MutableTypeNode::type).orElse(Type.of(Identifiers.EMPTY_IDENTIFIER));
	}
}