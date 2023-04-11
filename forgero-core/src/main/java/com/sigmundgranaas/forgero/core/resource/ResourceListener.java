package com.sigmundgranaas.forgero.core.resource;

import com.sigmundgranaas.forgero.core.type.TypeTree;

import java.util.Map;

@FunctionalInterface
public interface ResourceListener<T> {
	void listen(T resources, TypeTree tree, Map<String, String> idMapper);
}
