package com.sigmundgranaas.forgero.core.model;

import com.sigmundgranaas.forgero.core.texture.utils.Offset;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompositeModelTemplate implements ModelTemplate {
	private final List<ModelTemplate> models;

	public CompositeModelTemplate() {
		this.models = new ArrayList<>();
	}

	public void add(ModelTemplate template) {
		models.add(template);
	}

	public List<ModelTemplate> getModels() {
		return models;
	}

	@Override
	public int order() {
		return 0;
	}

	@Override
	public Optional<Offset> getOffset() {
		return Optional.empty();
	}

	@Override
	public <T> T convert(Converter<T, ModelTemplate> converter) {
		return converter.convert(this);
	}
}
