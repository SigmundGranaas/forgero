package com.sigmundgranaas.forgero.core.type;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.data.v2.data.TypeData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TypeTree {
    private final List<TypeNode> rootNodes;
    private List<TypeData> missingNodes;

    public TypeTree() {
        this.rootNodes = new ArrayList<>();
        this.missingNodes = new ArrayList<>();
    }

    public Optional<TypeNode> addNode(TypeData nodeData) {
        if (find(nodeData.name()).isEmpty()) {
            if (nodeData.parent().isPresent()) {
                return addNodeWithParent(nodeData.name(), nodeData.parent().get());
            } else {
                TypeNode node = new ParentLessTypeNode(ImmutableList.of(), nodeData.name());
                rootNodes.add(node);
                return Optional.of(node);
            }
        } else {
            ForgeroInitializer.LOGGER.warn("tried adding duplicated typeData to TypeTree, there might be duplication in your configuration");
            ForgeroInitializer.LOGGER.warn("tried adding: {}", nodeData.name());
        }
        return Optional.empty();
    }

    public void addNodes(List<TypeData> nodes) {
        nodes.forEach(this::addNode);
        this.resolve();
    }

    private Optional<TypeNode> addNodeWithParent(String name, String parent) {
        var parentOpt = find(parent);
        if (parentOpt.isPresent()) {
            TypeNode node = new ParentLessTypeNode(ImmutableList.<TypeNode>builder().build(), name);
            return Optional.of(parentOpt.get().addChild(node));
        } else {
            missingNodes.add(new TypeData(name, Optional.of(parent)));
        }
        return Optional.empty();
    }

    public void resolve() {
        if (this.missingNodes.isEmpty()) {
            return;
        }
        int missing = missingNodes.size();
        var addedNodes = missingNodes.stream().map(this::addNode).flatMap(Optional::stream).toList();
        removeMissingNodes(addedNodes);
        if (missing > addedNodes.size()) {
            resolve();
        }
    }

    private void removeMissingNodes(List<TypeNode> nodes) {
        this.missingNodes = new ArrayList<>(missingNodes.stream().filter(missingNode -> nodes.stream().noneMatch(node -> node.getName().equals(missingNode.name()))).toList());
    }

    Optional<TypeNode> find(String name) {
        return rootNodes.stream().map(typeNode -> find(name, typeNode)).flatMap(Optional::stream).findAny();
    }

    Optional<TypeNode> find(String name, TypeNode node) {
        if (node.getName().equals(name)) {
            return Optional.of(node);
        }
        return node.getChildren().stream().map(newNode -> find(name, newNode)).flatMap(Optional::stream).findAny();
    }
}
