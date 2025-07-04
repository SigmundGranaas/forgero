package com.sigmundgranaas.forgero.data.generator;

import com.sigmundgranaas.forgero.core.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.data.v3.dto.TopLevelData;

import java.util.Map;

public interface ComponentGenerator {
	Map<OpenIdentifier, TopLevelData> generate(Map<OpenIdentifier, TopLevelData> normalizedData, TagGraph tagGraph);
}
