package com.sigmundgranaas.forgero.resource;

import com.sigmundgranaas.forgero.type.TypeTree;

import java.util.Map;

@FunctionalInterface
public interface ResourceListener<T> {
    void listen(T resources, TypeTree tree, Map<String, String> idMapper);
}