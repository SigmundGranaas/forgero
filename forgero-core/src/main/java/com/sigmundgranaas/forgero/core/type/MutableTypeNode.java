package com.sigmundgranaas.forgero.core.type;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.util.TypeMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MutableTypeNode {

	private final String name;
	private final List<MutableTypeNode> children;
	private final HashMap<Class<?>, List<Object>> resourceMap;
	private List<MutableTypeNode> parent;

	public MutableTypeNode(@NotNull List<MutableTypeNode> children, @NotNull String name, @NotNull List<MutableTypeNode> parent) {
		this.parent = parent;
		this.children = children;
		this.name = name;
		this.resourceMap = new HashMap<>();
	}

	public List<MutableTypeNode> parent() {
		return parent;
	}

	public List<MutableTypeNode> children() {
		return children;
	}

	public List<MutableTypeNode> allChildren(List<MutableTypeNode> children) {
		children.addAll(children());
		children().forEach(child -> child.allChildren(children));
		return children;
	}

	public MutableTypeNode addChild(MutableTypeNode child) {
		MutableTypeNode parentedChild = child.addParent(this);
		this.children.add(child);
		return parentedChild;
	}

	public <T> void addResource(@NotNull Object resource, Class<T> type) {
		if (resourceMap.containsKey(type)) {
			resourceMap.get((type)).add(resource);
		} else {
			var arrayList = new ArrayList<>();
			arrayList.add(resource);
			resourceMap.put(type, arrayList);
		}
	}

	public <T> ImmutableList<T> getResources(Class<T> type) {
		var childrenResources = children.stream()
				.map(node -> node.getResources(type))
				.flatMap(List::stream)
				.collect(ImmutableList.toImmutableList());
		if (resourceMap.containsKey(type)) {
			var resources = Optional.ofNullable(resourceMap.get(type)).stream().flatMap(Collection::stream)
					.filter(type::isInstance)
					.map(type::cast)
					.toList();
			return ImmutableList.<T>builder().addAll(childrenResources).addAll(resources).build();
		}
		return childrenResources;
	}

	public MutableTypeNode addParent(MutableTypeNode parent) {
		this.parent = new ArrayList<>(this.parent);
		this.parent.add(parent);
		return this;
	}

	public TypeNode resolve() {
		var children = children().stream().map(MutableTypeNode::resolve).collect(ImmutableList.toImmutableList());
		var parentNode = new TypeNode(children, name, resourceMap);
		children.forEach(node -> node.link(parentNode));
		return parentNode;
	}

	public String name() {
		return this.name;
	}

	public Type type() {
		if (!parent.isEmpty()) {
			return new SimpleType(name, parent.stream().map(MutableTypeNode::type).toList(), new TypeMatcher());
		}
		return new SimpleType(name, Optional.empty(), new TypeMatcher());
	}
}
