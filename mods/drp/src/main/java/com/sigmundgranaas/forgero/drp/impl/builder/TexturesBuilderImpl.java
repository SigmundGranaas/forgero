package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.model.TexturesBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of TexturesBuilder.
 */
public class TexturesBuilderImpl implements TexturesBuilder {

	private final Map<String, String> textures = new HashMap<>();

	@Override
	public TexturesBuilder layer0(String textureId) {
		return layer(0, textureId);
	}

	@Override
	public TexturesBuilder layer1(String textureId) {
		return layer(1, textureId);
	}

	@Override
	public TexturesBuilder layer2(String textureId) {
		return layer(2, textureId);
	}

	@Override
	public TexturesBuilder layer(int layer, String textureId) {
		textures.put("layer" + layer, textureId);
		return this;
	}

	@Override
	public TexturesBuilder variable(String name, String textureId) {
		textures.put(name, textureId);
		return this;
	}

	@Override
	public TexturesBuilder particle(String textureId) {
		textures.put("particle", textureId);
		return this;
	}

	@Override
	public Map<String, String> getTextures() {
		return Map.copyOf(textures);
	}
}
