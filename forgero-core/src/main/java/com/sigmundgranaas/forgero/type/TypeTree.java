package com.sigmundgranaas.forgero.type;


import com.sigmundgranaas.forgero.util.Identifiers;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.resource.data.v2.data.TypeData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TypeTree implements UnresolvedTypeTree, MutableTypeTree {
    private final List<MutableTypeNode> rootNodes;
    private List<TypeData> missingNodes;

    public TypeTree() {
        this.rootNodes = new ArrayList<>();
        this.missingNodes = new ArrayList<>();
    }

    public Optional<MutableTypeNode> addNode(TypeData nodeData) {
        if (find(nodeData.name()).isEmpty()) {
            if (nodeData.parent().isPresent()) {
                return addNodeWithParent(nodeData.name(), nodeData.parent().get());
            } else {
                MutableTypeNode node = new MutableTypeNode(new ArrayList<>(), nodeData.name(), null);
                rootNodes.add(node);
                return Optional.of(node);
            }
        } else {
            Forgero.LOGGER.warn("tried adding duplicated typeData to TypeTree, there might be duplication in your configuration");
            Forgero.LOGGER.warn("tried adding: {}", nodeData.name());
        }
        return Optional.empty();
    }

    public void addNodes(List<TypeData> nodes) {
        nodes.forEach(this::addNode);
        this.resolve();
    }

    private Optional<MutableTypeNode> addNodeWithParent(String name, String parent) {
        var parentOpt = find(parent);
        if (parentOpt.isPresent()) {
            MutableTypeNode node = new MutableTypeNode(new ArrayList<>(), name, null);
            return Optional.of(parentOpt.get().addChild(node));
        } else {
            missingNodes.add(new TypeData(name, Optional.of(parent), Collections.emptyList()));
        }
        return Optional.empty();
    }

    public synchronized ResolvedTypeTree resolve() {
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
        return rootNodes.stream().map(MutableTypeNode::resolve).toList();
    }

    public List<MutableTypeNode> nodes() {
        var list = new ArrayList<>(rootNodes);
        rootNodes.forEach(node -> node.allChildren(list));
        return list;
    }

    private void removeMissingNodes(List<MutableTypeNode> nodes) {
        this.missingNodes = new ArrayList<>(missingNodes.stream().filter(missingNode -> nodes.stream().noneMatch(node -> node.name().equals(missingNode.name()))).toList());
    }

    public Optional<MutableTypeNode> find(String name) {
        return rootNodes.stream().map(typeNode -> find(name, typeNode).map(MutableTypeNode.class::cast)).flatMap(Optional::stream).findAny();
    }

    public Optional<MutableTypeNode> find(Type type) {
        return rootNodes.stream().map(typeNode -> find(type.typeName(), typeNode).map(MutableTypeNode.class::cast)).flatMap(Optional::stream).findAny();
    }

    public Optional<MutableTypeNode> find(String name, MutableTypeNode node) {
        if (node.name().equals(name)) {
            return Optional.of(node);
        }
        return node.children().stream().map(newNode -> find(name, newNode)).flatMap(Optional::stream).findAny();
    }

    public Type type(String type) {
        return find(type).map(MutableTypeNode::type).orElse(Type.of(Identifiers.EMPTY_IDENTIFIER));
    }
}
