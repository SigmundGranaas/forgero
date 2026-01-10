package com.sigmundgranaas.forgero.model.loading.impl.dto.templates;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.generation.impl.TemplateDataProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record UpgradeModelTemplateDTO(
		OpenIdentifier type,
		String context,
		TargetDTO target,
		List<TemplateModelDTO> models,
		Map<String, String> palette_map,
		Optional<String> palette_map_ref
) implements ModelTemplateDTO, TemplateDataProvider<TemplateModelDTO> {

	public UpgradeModelTemplateDTO(
			OpenIdentifier type,
			String context,
			TargetDTO target,
			List<TemplateModelDTO> models,
			Map<String, String> palette_map
	) {
		this(type, context, target, models, palette_map, Optional.empty());
	}

	@Override
	public Map<String, String> paletteMap() {
		return palette_map != null ? palette_map : Collections.emptyMap();
	}

	public UpgradeModelTemplateDTO withResolvedPaletteMap(Map<String, String> resolvedMap) {
		return new UpgradeModelTemplateDTO(type, context, target, models, resolvedMap, Optional.empty());
	}
}
