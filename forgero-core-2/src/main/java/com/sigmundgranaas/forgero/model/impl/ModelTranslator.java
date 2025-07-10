package com.sigmundgranaas.forgero.model.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.api.CompositeModel;
import com.sigmundgranaas.forgero.model.api.StaticModel;
import com.sigmundgranaas.forgero.model.impl.dto.CompositeModelDTO;
import com.sigmundgranaas.forgero.model.impl.dto.StaticModelDTO;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModelTranslator {
	public StaticModel translate(StaticModelDTO dto, OpenIdentifier id) { return new StaticModel(id, dto.texture(), dto.display()); }
	public CompositeModel translate(CompositeModelDTO dto, OpenIdentifier id) {
		List<CompositeModel.Self> self = (dto.self() != null) ? dto.self().stream().map(s -> new CompositeModel.Self(s.texture(), s.order())).collect(Collectors.toList()) : Collections.emptyList();
		List<CompositeModel.Part> parts = (dto.parts() != null) ? dto.parts().stream().map(p -> new CompositeModel.Part(p.slot(), p.order())).collect(Collectors.toList()) : Collections.emptyList();
		List<CompositeModel.Part> upgrades = (dto.upgrades() != null) ? dto.upgrades().stream().map(u -> new CompositeModel.Part(u.slot(), u.order())).collect(Collectors.toList()) : Collections.emptyList();
		List<CompositeModel.ModelSelector> selectors = (dto.model_selectors() != null) ? dto.model_selectors().stream().map(s -> new CompositeModel.ModelSelector(s.target_slot(), Optional.ofNullable(s.target_tag()), s.model())).collect(Collectors.toList()) : Collections.emptyList();
		return new CompositeModel(id, self, parts, upgrades, selectors, dto.display(), dto.overrides());
	}
}
