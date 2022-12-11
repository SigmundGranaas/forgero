package com.sigmundgranaas.forgero.resource.data;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.resource.data.v2.DataCollection;
import com.sigmundgranaas.forgero.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.resource.data.v2.data.ConstructData;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.resource.data.v2.factory.TypeFactory;
import com.sigmundgranaas.forgero.type.TypeTree;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;
import static com.sigmundgranaas.forgero.resource.data.v2.data.ResourceType.*;
import static com.sigmundgranaas.forgero.util.Identifiers.*;

@SuppressWarnings("ClassCanBeRecord")
public class DataPool {
    private final List<DataPackage> packages;
    private final Map<String, DataResource> resolvedResources;
    private final List<DataResource> finalResources;
    private final Map<String, DataResource> templates;
    private final TypeTree tree;
    private List<DataResource> unresolvedConstructs;

    public DataPool(List<DataPackage> packages) {
        this.packages = validatePackages(packages);
        this.resolvedResources = new HashMap<>();
        this.templates = new HashMap<>();
        this.unresolvedConstructs = new ArrayList<>();
        this.finalResources = new ArrayList<>();
        this.tree = assembleTypeTree();
    }

    private TypeTree assembleTypeTree() {
        TypeTree tree = new TypeTree();
        TypeFactory
                .convert(resources())
                .forEach(tree::addNode);
        tree.resolve();
        return tree;
    }

    public DataCollection assemble() {
        assembleStandaloneResources();
        assembleConstructs();
        return new DataCollection(tree, finalResources);
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
            int resolvedResources = 0;

            var temporaryResolved = unresolvedConstructs
                    .stream()
                    .filter(this::hasValidComponents)
                    .map(this::mapConstructTemplate)
                    .flatMap(List::stream)
                    .toList();

            for (DataResource resource : temporaryResolved) {
                unresolvedConstructs = unresolvedConstructs.stream().filter(res -> !res.identifier().equals(resource.identifier())).collect(Collectors.toList());
                if (resource.resourceType() != CONSTRUCT_TEMPLATE) {
                    addResource(resource);
                }
            }

            resolvedResources = temporaryResolved.size();
            if (resolvedResources == 0) {
                remainingResources = false;
            }
        }
    }

    private List<DataPackage> validatePackages(List<DataPackage> packages) {
        var packs = new HashSet<String>();
        packs.add("forgero");
        packs.add("minecraft");

        packages.forEach(pack -> packs.add(pack.name()));
        return packages.stream().filter(pack -> packs.containsAll(pack.dependencies())).toList();
    }

    private List<DataResource> mapConstructTemplate(DataResource resource) {
        var resources = new ArrayList<DataResource>();
        if (resource.construct().isEmpty()) {
            return Collections.emptyList();
        }
        var construct = resource.construct().get();
        if (construct.target().equals(THIS_IDENTIFIER)) {
            return List.of(resource);
        } else if (construct.target().equals(CREATE_IDENTIFIER)) {
            var constructs = mapConstructData(resource)
                    .stream()
                    .toList();
            resources.add(resource.toBuilder().construct(null).build());
            resources.addAll(constructs);
        }
        return resources;
    }

    private List<DataResource> mapConstructData(DataResource data) {
        List<DataResource> constructs = new ArrayList<>();
        if (data.construct().isEmpty()) {
            return Collections.emptyList();
        }
        var components = data.construct().get().components();
        var templateIngredients = new ArrayList<List<IngredientData>>();
        for (IngredientData ingredient : components) {
            if (ingredient.id().equals(THIS_IDENTIFIER)) {
                templateIngredients.add(List.of(IngredientData.builder().id(data.identifier()).build()));
            } else if (!ingredient.type().equals(EMPTY_IDENTIFIER)) {
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
                                    .map(element -> element.stream().filter(res -> res.resourceType() == DEFAULT)
                                            .toList())
                                    .orElse(Collections.emptyList());
                    var ingredients = resource
                            .stream()
                            .map(res -> IngredientData.builder().id(res.identifier()).build())
                            .toList();

                    templateIngredients.add(ingredients);
                }
            }
        }

        for (int i = 0; i < templateIngredients.get(0).size(); i++) {
            for (int j = 0; j < templateIngredients.get(1).size(); j++) {
                var builder = data.construct().get().toBuilder();
                var newComponents = new ArrayList<IngredientData>();
                newComponents.add(templateIngredients.get(0).get(i));
                newComponents.add(templateIngredients.get(1).get(j));
                builder.components(newComponents);
                String name = String.join(ELEMENT_SEPARATOR, newComponents.stream().map(IngredientData::id).map(this::idToName).toList());
                builder.target(THIS_IDENTIFIER);
                var construct = builder.build();
                var templateBuilder = Optional.ofNullable(templates.get(construct.type())).map(DataResource::toBuilder).orElse(DataResource.builder());
                if (hasDefaults(construct) || data.resourceType() == DEFAULT) {
                    templateBuilder.resourceType(DEFAULT);
                }
                constructs.add(templateBuilder
                        .construct(construct)
                        .namespace(data.nameSpace())
                        .name(name)
                        .type(construct.type())
                        .build());
            }
        }
        return constructs;
    }

    private boolean hasValidComponents(DataResource resource) {
        if (resource.construct().isEmpty()) {
            return false;
        }
        var construct = resource.construct().get();
        for (IngredientData data : construct.components()) {
            if (!data.id().equals(EMPTY_IDENTIFIER) && resolvedResources.containsKey(data.id())) {
                break;
            } else if (data.id().equals(THIS_IDENTIFIER)) {
                break;
            } else if (!data.type().equals(EMPTY_IDENTIFIER) && treeContainsTag(data.type())) {
                break;
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean hasDefaults(ConstructData data) {
        return data.components().stream().anyMatch(ingredient -> {
            if (ingredient.id().equals(EMPTY_IDENTIFIER)) {
                return false;
            } else {
                var res = Optional.ofNullable(resolvedResources.get(ingredient.id()));
                return res.filter(resource -> resource.resourceType() == DEFAULT).isPresent();
            }
        });
    }

    private boolean treeContainsTag(String tag) {
        return tree.find(tag).map(node -> !node.getResources(DataResource.class).isEmpty()).orElse(false);
    }

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
        return packages.stream().map(DataPackage::data).flatMap(List::stream).toList();
    }

    private String idToName(String id) {
        return id.split("#")[1];
    }

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
        return resource.resourceType() == CONSTRUCT_TEMPLATE;
    }

    private boolean isStatefulResource(DataResource resource) {
        if (resource.resourceType() == PACKAGE) {
            return false;
        } else if (resource.resourceType() == TYPE_DEFINITION) {
            return false;
        } else if (resource.name().equals(EMPTY_IDENTIFIER)) {
            return false;
        }
        return true;
    }
}