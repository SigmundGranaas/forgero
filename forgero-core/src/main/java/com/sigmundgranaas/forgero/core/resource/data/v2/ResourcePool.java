package com.sigmundgranaas.forgero.core.resource.data.v2;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ResourceType;
import com.sigmundgranaas.forgero.core.resource.data.v2.factory.TypeFactory;
import com.sigmundgranaas.forgero.core.state.Ingredient;
import com.sigmundgranaas.forgero.core.state.composite.Construct;
import com.sigmundgranaas.forgero.core.state.composite.Constructed;
import com.sigmundgranaas.forgero.core.type.ResolvedTypeTree;
import com.sigmundgranaas.forgero.core.type.TypeTree;
import com.sigmundgranaas.forgero.core.util.Identifiers;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ClassCanBeRecord")
public class ResourcePool {
    private final ImmutableList<DataResource> resources;
    private final TypeTree tree;
    private final List<DataResource> resolvedConstructs;
    private final List<DataResource> unresolvedConstructs;
    private final List<Constructed> constructs;

    public ResourcePool(ImmutableList<DataResource> resources) {
        this.resources = resources;
        this.resolvedConstructs = new ArrayList<>();
        this.unresolvedConstructs = new ArrayList<>();

        this.constructs = new ArrayList<>();
        this.tree = new TypeTree();
    }

    public ResolvedTypeTree createLoadedTypeTree() {
        new TypeFactory().convertJsonToData(resources).forEach(tree::addNode);
        tree.resolve();

        var resourceMap = resources.stream()
                .filter(resource -> Objects.nonNull(resource.name()) && !resource.name().equals(Identifiers.EMPTY_IDENTIFIER))
                .filter(resource -> resource.resourceType() != ResourceType.TYPE_DEFINITION)
                .collect(Collectors.groupingBy((DataResource::type), Collectors.mapping(resource -> resource, Collectors.toList())));

        resourceMap.forEach((entry, values) -> tree.find(entry).ifPresent(node -> values.stream()
                .map(this::resourceToIngredient)
                .flatMap(Optional::stream)
                .forEach(ingredient -> node.addResource(ingredient, Ingredient.class))));

        resources.stream().filter(resource -> resource.construct().isPresent()).forEach(unresolvedConstructs::add);

        boolean remainingResources = true;
        while (remainingResources) {
            int resolvedResources = 0;
            var temporaryResolved = new ArrayList<DataResource>();
            unresolvedConstructs.forEach(resource -> {
                assert resource.construct().isPresent();
                assert resource.construct().get().recipes().isPresent();
                var ingredients = resource.construct().get().recipes().get().get(0).ingredients();
                boolean resolved = true;
                var resourceIngredients = new ArrayList<Ingredient>();
                for (IngredientData ingredient : ingredients) {
                    int ingredientCount = resourceIngredients.size();
                    if (ingredient.id().equals("this")) {
                        resourceToIngredient(resource).ifPresent(resourceIngredients::add);
                    } else {
                        resourceIngredients.addAll(tree.find(ingredient.type())
                                .map(node -> node.getResources(Ingredient.class))
                                .orElse(ImmutableList.<Ingredient>builder().build()));

                    }
                    if (ingredientCount == resourceIngredients.size()) {
                        resolved = false;
                    }
                }
                if (resolved) {
                    temporaryResolved.add(resource);
                }
            });

            for (DataResource resource : temporaryResolved) {
                unresolvedConstructs.remove(resource);
                resolvedConstructs.add(resource);
            }

            resolvedConstructs.stream()
                    .map(this::constructToComposite)
                    .flatMap(List::stream)
                    .map(Ingredient::of)
                    .forEach(comp -> tree.find(comp.type().typeName()).ifPresent(node -> node.addResource(comp, Ingredient.class)));

            resolvedResources = temporaryResolved.size();
            if (resolvedResources == 0) {
                remainingResources = false;
            }
        }

        resolvedConstructs.stream()
                .map(this::constructToComposite)
                .flatMap(List::stream)
                .forEach(constructs::add);


        return tree.resolve();
    }

    private Optional<Ingredient> resourceToIngredient(DataResource resource) {
        if (resource.properties().isPresent()) {
            return Optional.of(Ingredient.of(resource.name(), tree.type(resource.type()), Collections.emptyList()));
        }
        return Optional.empty();
    }

    private List<Construct> constructToComposite(DataResource resource) {
        var jsonIngredients = resource.construct().get().recipes().get().get(0).ingredients();
        var templateIngredients = new ArrayList<List<Ingredient>>();
        for (IngredientData ingredient : jsonIngredients) {
            if (ingredient.id() != null && ingredient.id().equals("this")) {
                var thisIngredient = resourceToIngredient(resource).orElseThrow();
                templateIngredients.add(List.of(thisIngredient));
            } else if (ingredient.type() != null) {
                if (ingredient.unique()) {
                    var ingredients = tree.find(ingredient.type())
                            .map(node -> node.getResources(Ingredient.class))
                            .map(List::stream)
                            .map(Stream::toList)
                            .orElse(Collections.emptyList());
                    templateIngredients.add(ingredients);
                } else {
                    var ingredients =
                            tree.find(ingredient.type())
                                    .map(node -> node.getResources(Ingredient.class))
                                    .map(element -> element.stream().findFirst().map(List::of).orElse(Collections.emptyList()))
                                    .orElse(Collections.emptyList());

                    templateIngredients.add(ingredients);
                }
            }
        }

        var composites = new ArrayList<Construct>();
        for (int i = 0; i < templateIngredients.get(0).size(); i++) {
            for (int j = 0; j < templateIngredients.get(1).size(); j++) {
                var builder = Construct.builder();
                builder.addIngredient(templateIngredients.get(0).get(i));
                builder.type(tree.type(resource.construct().get().type()));
                builder.addIngredient(templateIngredients.get(1).get(j));
                composites.add(builder.build());
            }
        }
        return composites;
    }
}
