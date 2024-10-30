package com.sigmundgranaas.forgero.core.scope;

import com.sigmundgranaas.forgero.core.property.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupedAttributes implements Map<String, List<Attribute>> {
	private final Map<String, List<Attribute>> attributes;

	public GroupedAttributes() {
		this.attributes = new HashMap<>();
	}

	@Override
	public int size() {
		return attributes.size();
	}

	@Override
	public boolean isEmpty() {
		return attributes.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return attributes.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return attributes.containsValue(value);
	}

	@Override
	public List<Attribute> get(Object key) {
		return attributes.get(key);
	}

	@Override
	public @Nullable List<Attribute> put(String key, List<Attribute> value) {
		return attributes.put(key, value);
	}

	@Override
	public List<Attribute> remove(Object key) {
		return attributes.remove(key);
	}

	@Override
	public void putAll(@NotNull Map<? extends String, ? extends List<Attribute>> m) {
		attributes.putAll(m);
	}

	@Override
	public void clear() {
		attributes.clear();
	}

	@Override
	public @NotNull Set<String> keySet() {
		return attributes.keySet();
	}

	@Override
	public @NotNull Collection<List<Attribute>> values() {
		return attributes.values();
	}

	@Override
	public @NotNull Set<Entry<String, List<Attribute>>> entrySet() {
		return attributes.entrySet();
	}
}
