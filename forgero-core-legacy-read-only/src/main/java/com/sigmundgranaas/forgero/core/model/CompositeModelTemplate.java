package com.sigmundgranaas.forgero.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.texture.utils.Offset;
import org.jetbrains.annotations.NotNull;

public class CompositeModelTemplate implements ModelTemplate, Comparable<ModelTemplate> {
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
	
	@Override
	public int compareTo(@NotNull ModelTemplate o) {
		return 0;
	}
}
