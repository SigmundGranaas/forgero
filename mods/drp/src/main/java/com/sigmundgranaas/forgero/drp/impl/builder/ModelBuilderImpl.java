package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.model.ModelBuilder;
import com.sigmundgranaas.forgero.drp.api.model.ModelOverrideBuilder;
import com.sigmundgranaas.forgero.drp.api.model.TexturesBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Implementation of ModelBuilder.
 */
public class ModelBuilderImpl implements ModelBuilder {

	private String parent;
	private TexturesBuilder textures;
	private final List<ModelOverrideBuilder> overrides = new ArrayList<>();
	private final Map<String, DisplaySettings> display = new HashMap<>();

	@Override
	public ModelBuilder parent(String parent) {
		this.parent = parent;
		return this;
	}

	@Override
	public ModelBuilder textures(Consumer<TexturesBuilder> configurator) {
		TexturesBuilder builder = TexturesBuilder.create();
		configurator.accept(builder);
		this.textures = builder;
		return this;
	}

	@Override
	public ModelBuilder textures(TexturesBuilder textures) {
		this.textures = textures;
		return this;
	}

	@Override
	public ModelBuilder addOverride(Consumer<ModelOverrideBuilder> configurator) {
		ModelOverrideBuilder override = ModelOverrideBuilder.create();
		configurator.accept(override);
		overrides.add(override);
		return this;
	}

	@Override
	public ModelBuilder display(String displayType, float[] rotation, float[] translation, float[] scale) {
		display.put(displayType, new DisplaySettingsImpl(rotation, translation, scale));
		return this;
	}

	@Override
	public String getParent() {
		return parent;
	}

	@Override
	public TexturesBuilder getTextures() {
		return textures;
	}

	@Override
	public List<ModelOverrideBuilder> getOverrides() {
		return List.copyOf(overrides);
	}

	@Override
	public Map<String, DisplaySettings> getDisplay() {
		return Map.copyOf(display);
	}
}
