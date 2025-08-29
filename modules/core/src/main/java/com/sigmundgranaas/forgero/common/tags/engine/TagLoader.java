package com.sigmundgranaas.forgero.common.tags.engine;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TagLoader {
	private final IdentifierFactory factory;
	private final TagParser parser;

	public TagLoader(IdentifierFactory factory, TagParser parser) {
		this.factory = factory;
		this.parser = parser;
	}

	public TagGraph load(List<TagSource> sources) {
		TagGraphBuilder builder = new TagGraphBuilder();
		Map<String, TagDefinition> allDefinitions = new HashMap<>();

		for (TagSource source : sources) {
			source.getTags().forEach((tagName, jsonContent) -> {
				allDefinitions.put(tagName, parser.parse(jsonContent));
			});
		}

		allDefinitions.forEach((tagName, definition) -> {
			OpenIdentifier childId = factory.of(tagName);
			Set<OpenIdentifier> parentIds = definition.parents().stream()
					.map(factory::of)
					.collect(Collectors.toSet());
			builder.add(childId, parentIds);
		});

		return builder.build();
	}
}
