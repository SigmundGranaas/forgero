package com.sigmundgranaas.forgero.core.resource.data.processor;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;
import static com.sigmundgranaas.forgero.core.util.Identifiers.THIS_IDENTIFIER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.sigmundgranaas.forgero.core.identifier.Common;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ConstructData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ResourceType;
import com.sigmundgranaas.forgero.core.util.Identifiers;

/**
 * This class is responsible for inflating a schematic construct.
 * It takes a data resource, and a series of functions that find resources and provides templates,
 * to process the resource and create a list of data resources from its components.
 */
public class SchematicConstructInflater {

	private final DataResource resource;
	private final Function<String, List<DataResource>> typeFinder;
	private final Function<String, Optional<DataResource>> idFinder;
	private final Function<String, Optional<DataResource>> templateProvider;

	/**
	 * Initializes a new instance of the SchematicConstructInflater.
	 *
	 * @param resource         The DataResource to inflate.
	 * @param typeFinder       A function that given a type, returns a list of matching DataResources.
	 * @param idFinder         A function that given an id, returns the matching DataResource.
	 * @param templateProvider A function that given a type, returns a DataResource that can serve as a template.
	 */
	public SchematicConstructInflater(DataResource resource, Function<String, List<DataResource>> typeFinder, Function<String, Optional<DataResource>> idFinder, Function<String, Optional<DataResource>> templateProvider) {
		this.resource = resource;
		this.typeFinder = typeFinder;
		this.idFinder = idFinder;
		this.templateProvider = templateProvider;
	}

	/**
	 * Processes the DataResource and creates a list of data resources from its components.
	 *
	 * @return List of DataResources created from the components of the original DataResource.
	 */
	public List<DataResource> process() {
		if (invalidData()) {
			return Collections.emptyList();
		}
		var templateIngredients = inflateIngredients();
		return mapTemplateIngredients(templateIngredients);
	}

	/**
	 * Method to inflate the ingredients of a DataResource.
	 * For each ingredient in the resource, it checks if the ingredient is of type 'this', typed, or id,
	 * and adds the relevant IngredientData to the templateIngredients list.
	 *
	 * @return List of inflated IngredientData from the DataResource's components.
	 */
	private List<List<IngredientData>> inflateIngredients() {
		var templateIngredients = new ArrayList<List<IngredientData>>();
		var components = resource.construct().map(ConstructData::components).orElse(Collections.emptyList());
		for (IngredientData ingredient : components) {
			templateIngredients.add(getIngredientDataList(ingredient));
		}
		return templateIngredients;
	}

	/**
	 * Given an IngredientData, this method returns a corresponding list of IngredientData.
	 * The returned list is based on whether the IngredientData is 'this', typed or id.
	 *
	 * @param ingredient IngredientData to be processed.
	 * @return List of IngredientData corresponding to the given ingredient.
	 */
	private List<IngredientData> getIngredientDataList(IngredientData ingredient) {
		if (isThis(ingredient)) {
			return List.of(IngredientData.builder().id(resource.identifier()).unique(true).build());
		} else if (isTyped(ingredient)) {
			return ingredient.unique() ? findUniqueIngredients(ingredient.type()) : findDefaultIngredients(ingredient.type());
		} else if (isId(ingredient)) {
			return List.of(ingredient);
		}
		return Collections.emptyList();
	}

	/**
	 * Given a list of IngredientData lists, it maps each IngredientData into a DataResource.
	 * The new DataResource is created by adding all IngredientData from each list into the DataResource's components,
	 * and assigning the correct values to the DataResource's other attributes.
	 *
	 * @param templateIngredients List of IngredientData lists to be mapped to DataResources.
	 * @return List of DataResources mapped from the given IngredientData lists.
	 */
	private List<DataResource> mapTemplateIngredients(List<List<IngredientData>> templateIngredients) {
		List<DataResource> constructs = new ArrayList<>();

		List<List<IngredientData>> product = Lists.cartesianProduct(templateIngredients);  // Use Guava's cartesianProduct to support any number of components
		for (List<IngredientData> combination : product) {
			constructs.add(buildDataResource(combination));
		}

		return constructs;
	}

	/**
	 * Given a list of IngredientData, it builds a new DataResource by adding the IngredientData list into the DataResource's components,
	 * and assigning the correct values to the DataResource's other attributes.
	 *
	 * @param components IngredientData list to be added to the new DataResource's components.
	 * @return The newly built DataResource.
	 */
	private DataResource buildDataResource(List<IngredientData> components) {
		var builder = resource.construct().get().toBuilder();
		builder.components(components);
		String name = String.join(Common.ELEMENT_SEPARATOR, components.stream().map(IngredientData::id).map(this::idToName).toList());
		builder.target(Identifiers.THIS_IDENTIFIER);
		var construct = builder.build();
		var templateBuilder = templateProvider.apply(construct.type()).map(DataResource::toBuilder).orElse(DataResource.builder());
		if (hasDefaults(construct) || resource.resourceType() == ResourceType.DEFAULT || name.equals("handle-schematic-oak") || name.equals("crossbow_stock-schematic-oak")) {
			templateBuilder.resourceType(ResourceType.DEFAULT);
		}
		return templateBuilder
				.construct(construct)
				.namespace(resource.nameSpace())
				.container(resource.container().get())
				.name(name)
				.type(construct.type())
				.build();
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
