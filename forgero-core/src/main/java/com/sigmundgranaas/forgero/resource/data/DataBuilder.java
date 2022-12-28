package com.sigmundgranaas.forgero.resource.data;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.identifier.Common;
import com.sigmundgranaas.forgero.resource.data.v2.data.*;
import com.sigmundgranaas.forgero.type.TypeTree;
import com.sigmundgranaas.forgero.util.Identifiers;
import com.sigmundgranaas.forgero.resource.data.v2.data.ConstructData;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.resource.data.v2.data.RecipeData;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("DuplicatedCode")
public class DataBuilder {
    private final Map<String, DataResource> resolvedResources;
    private final List<DataResource> finalResources;
    private final Map<String, DataResource> templates;
    private final TypeTree tree;
    private List<DataResource> resources;

    private List<RecipeData> recipes;
    private List<DataResource> unresolvedConstructs;

    public DataBuilder(List<DataResource> resources, TypeTree tree) {
        this.resources = resources;
        this.resolvedResources = new HashMap<>();
        this.templates = new HashMap<>();
        this.unresolvedConstructs = new ArrayList<>();
        this.finalResources = new ArrayList<>();
        this.recipes = new ArrayList<>();
        this.tree = tree;
    }

    public static DataBuilder of(List<DataResource> resources, TypeTree tree) {
        return new DataBuilder(resources, tree);
    }

    public List<DataResource> buildResources() {
        mapParentResources();
        assembleStandaloneResources();
        assembleConstructs();
        return finalResources;
    }

    public List<RecipeData> recipes() {
        return recipes;
    }

    private void mapParentResources() {
        var namedResources = resources.stream().collect(Collectors.toMap((DataResource::name), (dataResource -> dataResource), (present, newRes) -> newRes));
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
        }
    }

    private List<DataResource> mapConstructTemplate(DataResource resource) {
        var resources = new ArrayList<DataResource>();
        if (resource.construct().isEmpty()) {
            return Collections.emptyList();
        }
        var construct = resource.construct().get();
        if (construct.target().equals(Identifiers.THIS_IDENTIFIER)) {
            return List.of(resource);
        } else if (construct.target().equals(Identifiers.CREATE_IDENTIFIER)) {
            var constructs = mapConstructData(resource)
                    .stream()
                    .toList();
            resources.add(resource.toBuilder().construct(null).build());
            resources.addAll(constructs);
        }
        return resources;
    }

    private List<DataResource> mapConstructData(DataResource data) {
        if (data.construct().isPresent() && data.construct().get().recipes().isPresent()) {
            data.construct().get().recipes().get().stream()
                    .map(recipe -> inflateRecipes(recipe, data))
                    .flatMap(List::stream)
                    .forEach(recipes::add);
        }
        List<DataResource> constructs = new ArrayList<>();
        if (data.construct().isEmpty()) {
            return Collections.emptyList();
        }
        var components = data.construct().get().components();
        var templateIngredients = new ArrayList<List<IngredientData>>();
        for (IngredientData ingredient : components) {
            if (ingredient.id().equals(Identifiers.THIS_IDENTIFIER)) {
                templateIngredients.add(List.of(IngredientData.builder().id(data.identifier()).unique(true).build()));
            } else if (!ingredient.type().equals(Identifiers.EMPTY_IDENTIFIER)) {
                if (ingredient.unique()) {
                    var resources = tree.find(ingredient.type())
                            .map(node -> node.getResources(DataResource.class))
                            .map(List::stream)
                            .map(Stream::toList)
                            .orElse(Collections.emptyList());

                    var ingredients = resources
                            .stream()
                            .map(res -> IngredientData.builder().id(res.identifier()).unique(true).build())
                            .toList();

                    templateIngredients.add(ingredients);
                } else {

                    var resource =
                            tree.find(ingredient.type())
                                    .map(node -> node.getResources(DataResource.class))
                                    .map(element -> element.stream().filter(res -> res.resourceType() == ResourceType.DEFAULT)
                                            .toList())
                                    .orElse(Collections.emptyList());
                    var ingredients = resource
                            .stream()
                            .map(res -> IngredientData.builder().id(res.identifier()).build())
                            .toList();

                    templateIngredients.add(ingredients);
                }


            } else if (!ingredient.id().equals(Identifiers.EMPTY_IDENTIFIER)) {
                templateIngredients.add(List.of(ingredient));
            }
        }

        for (int i = 0; i < templateIngredients.get(0).size(); i++) {
            for (int j = 0; j < templateIngredients.get(1).size(); j++) {
                var builder = data.construct().get().toBuilder();
                var newComponents = new ArrayList<IngredientData>();
                newComponents.add(templateIngredients.get(0).get(i));
                newComponents.add(templateIngredients.get(1).get(j));
                builder.components(newComponents);
                String name = String.join(Common.ELEMENT_SEPARATOR, newComponents.stream().map(IngredientData::id).map(this::idToName).toList());
                builder.target(Identifiers.THIS_IDENTIFIER);
                var construct = builder.build();
                var templateBuilder = Optional.ofNullable(templates.get(construct.type())).map(DataResource::toBuilder).orElse(DataResource.builder());
                if (hasDefaults(construct) || data.resourceType() == ResourceType.DEFAULT || name.equals("handle-schematic-oak")) {
                    templateBuilder.resourceType(ResourceType.DEFAULT);
                }
                constructs.add(templateBuilder
                        .construct(construct)
                        .namespace(data.nameSpace())
                        .container(data.container().get())
                        .name(name)
                        .type(construct.type())
                        .build());
            }
        }
        return constructs;
    }


    private List<RecipeData> inflateRecipes(RecipeData data, DataResource rootResource) {
        var recipes = new ArrayList<RecipeData>();

        var rootIngredients = data.ingredients();
        var templateIngredients = new ArrayList<List<IngredientData>>();
        for (IngredientData ingredient : rootIngredients) {
            if (ingredient.id().equals(Identifiers.THIS_IDENTIFIER)) {
                templateIngredients.add(List.of(IngredientData.builder().id(rootResource.identifier()).unique(true).build()));
            } else if (!ingredient.type().equals(Identifiers.EMPTY_IDENTIFIER)) {
                if (ingredient.unique()) {
                    var resources = tree.find(ingredient.type())
                            .map(node -> node.getResources(DataResource.class))
                            .map(List::stream)
                            .map(Stream::toList)
                            .orElse(Collections.emptyList());

                    var ingredients = resources
                            .stream()
                            .map(res -> IngredientData.builder()
                                    .id(res.identifier())
                                    .unique(true)
                                    .amount(ingredient.amount())
                                    .build())
                            .toList();

                    templateIngredients.add(ingredients);
                } else {
                    var resource =
                            tree.find(ingredient.type())
                                    .map(node -> node.getResources(DataResource.class))
                                    .map(element -> element.stream().filter(res -> res.resourceType() == ResourceType.DEFAULT)
                                            .toList())
                                    .orElse(Collections.emptyList());
                    var ingredients = resource
                            .stream()
                            .map(res -> IngredientData.builder()
                                    .id(res.identifier())
                                    .amount(ingredient.amount())
                                    .build())
                            .toList();

                    templateIngredients.add(ingredients);
                }
            }
        }
        for (int i = 0; i < templateIngredients.get(0).size(); i++) {
            for (int j = 0; j < templateIngredients.get(1).size(); j++) {
                var newComponents = new ArrayList<IngredientData>();
                newComponents.add(templateIngredients.get(0).get(i));
                newComponents.add(templateIngredients.get(1).get(j));
                String name = String.join(Common.ELEMENT_SEPARATOR, newComponents.stream().map(IngredientData::id).map(this::idToName).toList());
                recipes.add(RecipeData.builder()
                        .ingredients(newComponents)
                        .craftingType(data.type())
                        .target(rootResource.nameSpace() + ":" + name)
                        .build());
            }
        }
        return recipes;
    }


    private boolean hasValidComponents(DataResource resource) {
        if (resource.construct().isEmpty()) {
            return false;
        }
        var construct = resource.construct().get();
        for (IngredientData data : construct.components()) {
            if (!data.id().equals(Identifiers.EMPTY_IDENTIFIER) && resolvedResources.containsKey(data.id())) {
                break;
            } else if (data.id().equals(Identifiers.THIS_IDENTIFIER)) {
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

    private String idToName(String id) {
        String[] split = id.split(":");
        if (split.length > 1) {
            return split[1];
        }
        return id;
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
