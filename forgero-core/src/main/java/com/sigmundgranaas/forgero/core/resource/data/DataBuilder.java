package com.sigmundgranaas.forgero.core.resource.data;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.resource.data.processor.RecipeInflater;
import com.sigmundgranaas.forgero.core.resource.data.processor.SchematicConstructInflater;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.*;
import com.sigmundgranaas.forgero.core.type.TypeTree;
import com.sigmundgranaas.forgero.core.util.Identifiers;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sigmundgranaas.forgero.core.util.Identifiers.THIS_IDENTIFIER;

@SuppressWarnings("DuplicatedCode")
public class DataBuilder {
	private final Map<String, DataResource> resolvedResources;
	private final List<DataResource> finalResources;
	private final Map<String, DataResource> templates;
	private final TypeTree tree;
	private Set<String> typeDependencySet = new HashSet<>();
	private List<DataResource> resources;
	private List<DataResource> overrides;
	private Map<String, List<DataResource>> mappedOverrides = new HashMap<>();

	private List<RecipeData> recipes;
	private List<DataResource> unresolvedConstructs;

	public DataBuilder(List<DataResource> resources, List<DataResource> overrides, TypeTree tree) {
		this.resources = resources;
		this.overrides = overrides;
		this.resolvedResources = new HashMap<>();
		this.templates = new HashMap<>();
		this.unresolvedConstructs = new ArrayList<>();
		this.finalResources = new ArrayList<>();
		this.recipes = new ArrayList<>();
		this.tree = tree;
	}

	public static DataBuilder of(List<DataResource> resources, List<DataResource> overrides, TypeTree tree) {
		return new DataBuilder(resources, overrides, tree);
	}

	public List<DataResource> buildResources() {
		this.mappedOverrides = mapOverrides(this.overrides);
		mapParentResources();
		assembleStandaloneResources();
		assembleConstructs();
		return finalResources;
	}

	private Map<String, List<DataResource>> mapOverrides(List<DataResource> overrides) {
		Map<String, List<DataResource>> result = overrides.stream()
				.filter(this::validateOverrides)
				.collect(Collectors.groupingBy(DataResource::identifier));

		if(!result.isEmpty()){
			Forgero.LOGGER.info("Validated overrides for {} resources", result.size());
		}

		return result;
	}

	public List<RecipeData> recipes() {
		return recipes;
	}

	private void mapParentResources() {
		Map<String, DataResource> namedResources = resources.stream()
				.map(res -> applyOverride(this.mappedOverrides, res))
				.collect(Collectors.toMap((DataResource::name), (dataResource -> dataResource), (present, newRes) -> newRes));
		namedResources.remove(Identifiers.EMPTY_IDENTIFIER);
		this.resources = namedResources.values().stream()
				.map(res -> applyParent(namedResources, res))
				.filter(this::notAbstract)
				.toList();
	}

	private DataResource applyParent(Map<String, DataResource> resources, DataResource resource) {
		if (hasParent(resource)) {
			var parent = Optional.ofNullable(resources.get(resource.parent()));
			if (parent.isPresent()) {
				return resource.mergeResource(applyParent(resources, parent.get()));
			} else {
				return resource;
			}
		} else {
			return resource;
		}
	}

	/**
	 * Applies overrides according to their order if they exist.
	 * Currently only supports adding or overriding properties.
	 *
	 * @param mappedOverrides Map of overrides grouped by identifier
	 * @param resource 	      Resource to apply overrides to
	 * @return Resource with overrides applied if applicable
	 */
	private DataResource applyOverride(Map<String, List<DataResource>> mappedOverrides, DataResource resource) {
		if(mappedOverrides.containsKey(resource.identifier())){
			return mappedOverrides.get(resource.identifier()).stream()
					// Overrides with higher priority will be applied last.
					.sorted(Comparator.comparingInt(DataResource::priority).reversed())
					.reduce(DataResource::mergeResource)
					.map(resource::mergeResource)
					.orElse(resource);
		}else{
			return resource;
		}
	}

	/**
	 * Validates if the override resource has the correct fields to override the resource.
	 * Overrides needs to contain the following: name, namespace, properties.
	 * <p>
	 * Not having a name or namespace will result in the override being ignored.
	 * Not having properties will result in the override being ignored, as there is nothing to override.
	 * <p>
	 * Will log an error if the override is invalid.
	 * @param resource Resource to validate
	 * @return True if the override is valid, false if not.
	 */
	private boolean validateOverrides(DataResource resource) {
		if (resource.priority() < 1) {
			Forgero.LOGGER.warn("0 is the default priority for resources, please set it higher for overrides. Current priority: {}", resource.priority());
			logResourceDataForDebug(resource);
			return false;
		}

		if (resource.construct().isPresent()) {
			Forgero.LOGGER.warn("Overrides cannot contain constructs");
			logResourceDataForDebug(resource);
			return false;
		}

		if (resource.name().equals(Identifiers.EMPTY_IDENTIFIER)) {
			Forgero.LOGGER.warn("Invalid name in override: {}", resource.identifier());
			logResourceDataForDebug(resource);
			return false;
		} else if (resource.nameSpace().equals(Identifiers.EMPTY_IDENTIFIER)) {
			Forgero.LOGGER.warn("Invalid namespace in override: {}", resource.identifier());
			logResourceDataForDebug(resource);
			return false;
		} else if (resource.properties().isEmpty()) {
			Forgero.LOGGER.warn("Empty properties in override: {}", resource.identifier());
			logResourceDataForDebug(resource);
			return false;
		}

		return true;
	}

	private void logResourceDataForDebug(DataResource resource){
		Forgero.LOGGER.debug("Debug data for resource file:");
		Forgero.LOGGER.debug("Resource: {}", resource.identifier());
		Forgero.LOGGER.debug("Name: {}", resource.name());
		Forgero.LOGGER.debug("Namespace: {}", resource.nameSpace());
		Forgero.LOGGER.debug("Properties: {}", resource.properties());
	}

	private void assembleConstructs() {
		resources()
				.stream()
				.filter(this::isConstruct)
				.forEach(unresolvedConstructs::add);

		resources()
				.stream()
				.filter(this::isTemplate)
				.forEach(res -> templates.put(res.identifier(), res));
		boolean remainingResources = true;


		while (remainingResources) {
			int resolvedResources;
			unresolvedConstructs.stream().map(DataResource::construct).flatMap(Optional::stream).map(ConstructData::type).forEach(typeDependencySet::add);
			var temporaryResolved = unresolvedConstructs
					.stream()
					.filter(this::hasValidComponents)
					.map(this::mapConstructTemplate)
					.flatMap(List::stream)
					.toList();

			for (DataResource resource : temporaryResolved) {
				unresolvedConstructs = unresolvedConstructs.stream().filter(res -> !res.identifier().equals(resource.identifier())).collect(Collectors.toList());
				if (resource.resourceType() != ResourceType.CONSTRUCT_TEMPLATE) {
					addResource(resource);
				}
			}

			resolvedResources = temporaryResolved.size();
			if (resolvedResources == 0) {
				remainingResources = false;
			}
			typeDependencySet.clear();
		}
	}

	private List<DataResource> mapConstructTemplate(DataResource resource) {
		var resources = new ArrayList<DataResource>();
		if (resource.construct().isEmpty()) {
			return Collections.emptyList();
		}
		var construct = resource.construct().get();
		if (construct.target().equals(THIS_IDENTIFIER)) {
			return List.of(resource);
		} else if (construct.target().equals(Identifiers.CREATE_IDENTIFIER)) {
			var constructs = mapConstructData(resource);
			resources.add(resource.toBuilder().construct(null).build());
			resources.addAll(constructs);
		}
		return resources;
	}

	private List<DataResource> mapConstructData(DataResource data) {
		Function<String, Optional<DataResource>> idFinder = (String id) -> Optional.ofNullable(resolvedResources.get(id));
		Function<String, Optional<DataResource>> templateFinder = (String id) -> Optional.ofNullable(templates.get(id));

		if (data.construct().isPresent() && data.construct().get().recipes().isPresent()) {
			var inflatedRecipes = new RecipeInflater(data, this::findResourceFromType, idFinder, templateFinder)
					.process();
			recipes.addAll(inflatedRecipes);
		}
		if (data.construct().isEmpty()) {
			return Collections.emptyList();
		}
		var inflater = new SchematicConstructInflater(data, this::findResourceFromType, idFinder, templateFinder);
		return inflater.process();
	}

	private List<DataResource> findResourceFromType(String type) {
		return tree.find(type)
				.map(node -> node.getResources(DataResource.class))
				.orElse(ImmutableList.<DataResource>builder().build());
	}

	private boolean hasValidComponents(DataResource resource) {
		if (resource.construct().isEmpty()) {
			return false;
		}
		var construct = resource.construct().get();
		for (IngredientData data : construct.components()) {
			if (!data.type().equals(Identifiers.EMPTY_IDENTIFIER) && typeDependencySet.contains(data.type())) {
				return false;
			} else if (!data.id().equals(Identifiers.EMPTY_IDENTIFIER) && resolvedResources.containsKey(data.id())) {
				break;
			} else if (data.id().equals(THIS_IDENTIFIER)) {
				break;
			} else if (!data.type().equals(Identifiers.EMPTY_IDENTIFIER) && treeContainsTag(data.type())) {
				break;
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean notAbstract(DataResource resource) {
		return resource.resourceType() != ResourceType.ABSTRACT;

	}

	private boolean hasDefaults(ConstructData data) {
		return data.components().stream().allMatch(ingredient -> {
			if (ingredient.id().equals(Identifiers.EMPTY_IDENTIFIER)) {
				return false;
			} else if (ingredient.id().equals("handle_schematic")) {
				return true;
			} else {
				var res = Optional.ofNullable(resolvedResources.get(ingredient.id()));
				return res.filter(resource -> resource.resourceType() == ResourceType.DEFAULT).isPresent();
			}
		});
	}

	private boolean hasParent(DataResource data) {
		return !data.parent().equals(Identifiers.EMPTY_IDENTIFIER) && !data.name().equals(Identifiers.EMPTY_IDENTIFIER) && isStatefulResource(data);
	}

	private boolean treeContainsTag(String tag) {
		return tree.find(tag).map(node -> !node.getResources(DataResource.class).isEmpty()).orElse(false);
	}

	@SuppressWarnings("unused")
	private ImmutableList<DataResource> resourcesFromTag(String tag) {
		return tree.find(tag).map(node -> node.getResources(DataResource.class)).orElse(ImmutableList.<DataResource>builder().build());
	}

	private void assembleStandaloneResources() {
		resources()
				.stream()
				.filter(this::isIndependentResource)
				.forEach(this::addResource);
	}

	private List<DataResource> resources() {
		return resources;
	}

	@SuppressWarnings("unused")
	private String idToNameSpace(String id) {
		return id.split("#")[0];
	}

	private void addResource(DataResource resource) {
		resolvedResources.put(resource.identifier(), resource);
		finalResources.add(resource);
		tree.find(resource.type()).ifPresent(node -> node.addResource(resource, DataResource.class));
	}

	private boolean isIndependentResource(DataResource resource) {
		return resource.construct().isEmpty() && isStatefulResource(resource) && resource.properties().isPresent();
	}

	private boolean isConstruct(DataResource resource) {
		return resource.construct().isPresent() && isStatefulResource(resource);
	}

	private boolean isTemplate(DataResource resource) {
		return resource.resourceType() == ResourceType.CONSTRUCT_TEMPLATE;
	}

	@SuppressWarnings("RedundantIfStatement")
	private boolean isStatefulResource(DataResource resource) {
		if (resource.resourceType() == ResourceType.PACKAGE) {
			return false;
		} else if (resource.resourceType() == ResourceType.TYPE_DEFINITION) {
			return false;
		} else if (resource.name().equals(Identifiers.EMPTY_IDENTIFIER)) {
			return false;
		}
		return true;
	}
}
