package com.sigmundgranaas.forgero.core.resource.data.processor;

import com.sigmundgranaas.forgero.core.identifier.Common;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.*;
import com.sigmundgranaas.forgero.core.util.Identifiers;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;
import static com.sigmundgranaas.forgero.core.util.Identifiers.THIS_IDENTIFIER;

public class SchematicConstructInflater {
	private final DataResource resource;

	private final Function<String, List<DataResource>> typeFinder;

	private final Function<String, Optional<DataResource>> idFinder;

	private final Function<String, Optional<DataResource>> templateProvider;

	public SchematicConstructInflater(DataResource resource, Function<String, List<DataResource>> typeFinder, Function<String, Optional<DataResource>> idFinder, Function<String, Optional<DataResource>> templateProvider) {
		this.resource = resource;
		this.typeFinder = typeFinder;
		this.idFinder = idFinder;
		this.templateProvider = templateProvider;
	}

	public List<DataResource> process() {
		if (invalidData()) {
			return Collections.emptyList();
		}
		var templateIngredients = inflateIngredients();

		return mapTemplateIngredients(templateIngredients);
	}

	private List<List<IngredientData>> inflateIngredients() {
		var templateIngredients = new ArrayList<List<IngredientData>>();
		var components = resource.construct().map(ConstructData::components).orElse(Collections.emptyList());
		for (IngredientData ingredient : components) {
			if (isThis(ingredient)) {
				templateIngredients.add(List.of(IngredientData.builder().id(resource.identifier()).unique(true).build()));
			} else if (isTyped(ingredient)) {
				if (ingredient.unique()) {
					templateIngredients.add(findUniqueIngredients(ingredient.type()));
				} else {
					templateIngredients.add(findDefaultIngredients(ingredient.type()));
				}
			} else if (isId(ingredient)) {
				templateIngredients.add(List.of(ingredient));
			}
		}
		return templateIngredients;
	}

	private List<DataResource> mapTemplateIngredients(List<List<IngredientData>> templateIngredients) {
		List<DataResource> constructs = new ArrayList<>();

		for (int i = 0; i < templateIngredients.get(0).size(); i++) {
			for (int j = 0; j < templateIngredients.get(1).size(); j++) {
				var builder = resource.construct().get().toBuilder();
				var newComponents = new ArrayList<IngredientData>();
				newComponents.add(templateIngredients.get(0).get(i));
				newComponents.add(templateIngredients.get(1).get(j));
				builder.components(newComponents);
				String name = String.join(Common.ELEMENT_SEPARATOR, newComponents.stream().map(IngredientData::id).map(this::idToName).toList());
				builder.target(Identifiers.THIS_IDENTIFIER);
				var construct = builder.build();
				var templateBuilder = templateProvider.apply(construct.type()).map(DataResource::toBuilder).orElse(DataResource.builder());
				if (hasDefaults(construct) || resource.resourceType() == ResourceType.DEFAULT || name.equals("handle-schematic-oak")) {
					templateBuilder.resourceType(ResourceType.DEFAULT);
				}
				constructs.add(templateBuilder
						.construct(construct)
						.namespace(resource.nameSpace())
						.container(resource.container().get())
						.name(name)
						.type(construct.type())
						.build());
			}
		}
		return constructs;
	}

	private String idToName(String id) {
		String[] split = id.split(":");
		if (split.length > 1) {
			return split[1];
		}
		return id;
	}

	private boolean hasDefaults(ConstructData data) {
		return data.components().stream().allMatch(ingredient -> {
			if (ingredient.id().equals(Identifiers.EMPTY_IDENTIFIER)) {
				return false;
			} else if (ingredient.id().equals("handle_schematic")) {
				return true;
			} else {
				var res = idFinder.apply(ingredient.id());
				return res.filter(resource -> resource.resourceType() == ResourceType.DEFAULT).isPresent();
			}
		});
	}

	private boolean isTyped(IngredientData data) {
		return !data.type().equals(EMPTY_IDENTIFIER);
	}

	private boolean isId(IngredientData data) {
		return !data.id().equals(EMPTY_IDENTIFIER);
	}

	private boolean isThis(IngredientData data) {
		return isId(data) && data.id().equals(THIS_IDENTIFIER);
	}


	private boolean invalidData() {
		return resource.construct().isEmpty();
	}

	private List<IngredientData> findUniqueIngredients(String type) {
		return typeFinder.apply(type).stream()
				.map(res -> IngredientData.builder().id(res.identifier()).unique(true).build())
				.toList();
	}

	private List<IngredientData> findDefaultIngredients(String type) {
		return typeFinder.apply(type).stream()
				.filter(res -> res.resourceType() == ResourceType.DEFAULT)
				.map(res -> IngredientData.builder().id(res.identifier()).unique(true).build())
				.toList();
	}

	public Set<String> dependencies() {
		return resource.construct()
				.map(ConstructData::components)
				.map(list -> list.stream()
						.map(ingredient -> ingredient.id().equals(EMPTY_IDENTIFIER) ? ingredient.id() : ingredient.type())
						.collect(Collectors.toSet()))
				.orElse(Collections.emptySet());
	}
}
