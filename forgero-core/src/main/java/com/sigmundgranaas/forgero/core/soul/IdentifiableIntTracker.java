package com.sigmundgranaas.forgero.core.soul;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class IdentifiableIntTracker {
    private final Map<String, Integer> idMap;

    public IdentifiableIntTracker() {
        this.idMap = new ConcurrentHashMap<>();
    }

    public IdentifiableIntTracker(Map<String, Integer> idMap) {
        this.idMap = idMap;
    }

    public int getTotal() {
        return idMap.values().stream().reduce(0, Integer::sum);
    }

    public void add(String id) {
        add(id, 1);
    }

    public void add(String id, int amount) {
        if (idMap.containsKey(id)) {
            idMap.compute(id, (currentId, oldValue) -> Objects.requireNonNullElse(oldValue, 0) + amount);
        } else {
            idMap.put(id, amount);
        }
    }

    public int getValueFor(String id) {
        return idMap.getOrDefault(id, 0);
    }

    public Map<String, Integer> toMap() {
        return ImmutableMap.copyOf(idMap);
    }
}
