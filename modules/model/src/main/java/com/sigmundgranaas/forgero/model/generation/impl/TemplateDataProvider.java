package com.sigmundgranaas.forgero.model.generation.impl;

import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.TargetDTO;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface TemplateDataProvider<T> {
	TargetDTO target();
	List<T> models();

	default Map<String, String> paletteMap() {
		return Collections.emptyMap();
	}
}
