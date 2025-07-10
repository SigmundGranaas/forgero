package com.sigmundgranaas.forgero.model.impl.dto;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public record CompositeModelDTO(
		String model_type,
		@Nullable List<SelfDTO> self,
		@Nullable List<PartDTO> parts,
		@Nullable List<PartDTO> upgrades,
		@Nullable List<ModelSelectorDTO> model_selectors,
		@Nullable JsonElement display,
		@Nullable List<JsonElement> overrides
) implements ModelDTO {}
