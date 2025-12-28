package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.lang.LanguageBuilder;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of LanguageBuilder.
 */
public class LanguageBuilderImpl implements LanguageBuilder {

	private final Map<String, String> entries = new HashMap<>();

	@Override
	public LanguageBuilder item(Identifier itemId, String translation) {
		String key = "item." + itemId.getNamespace() + "." + itemId.getPath().replace('/', '.');
		entries.put(key, translation);
		return this;
	}

	@Override
	public LanguageBuilder block(Identifier blockId, String translation) {
		String key = "block." + blockId.getNamespace() + "." + blockId.getPath().replace('/', '.');
		entries.put(key, translation);
		return this;
	}

	@Override
	public LanguageBuilder entity(Identifier entityId, String translation) {
		String key = "entity." + entityId.getNamespace() + "." + entityId.getPath().replace('/', '.');
		entries.put(key, translation);
		return this;
	}

	@Override
	public LanguageBuilder tooltip(String key, String translation) {
		entries.put(key, translation);
		return this;
	}

	@Override
	public LanguageBuilder add(String key, String translation) {
		entries.put(key, translation);
		return this;
	}

	@Override
	public LanguageBuilder merge(LanguageBuilder other) {
		entries.putAll(other.getEntries());
		return this;
	}

	@Override
	public Map<String, String> getEntries() {
		return Map.copyOf(entries);
	}
}
