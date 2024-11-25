package com.sigmundgranaas.forgero.dynamicresourcepack.api.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class LangResource {
	public static final Codec<LangResource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("translations").forGetter(LangResource::getTranslations)
	).apply(instance, LangResource::new));

	private final Map<String, String> translations;

	public LangResource(Map<String, String> translations) {
		this.translations = new HashMap<>(translations);
	}

	public LangResource() {
		this(new HashMap<>());
	}

	public Map<String, String> getTranslations() {
		return translations;
	}

	public void item(Identifier id, String name) {
		translations.put("item." + id.getNamespace() + "." + id.getPath(), name);
	}

	public void block(Identifier id, String name) {
		translations.put("block." + id.getNamespace() + "." + id.getPath(), name);
	}
}
