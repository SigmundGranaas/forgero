package com.sigmundgranaas.forgero.core.data.v2;

import com.google.common.collect.ImmutableList;
import com.sigmundgranaas.forgero.core.data.v2.factory.TypeFactory;
import com.sigmundgranaas.forgero.core.data.v2.json.JsonIngredient;
import com.sigmundgranaas.forgero.core.data.v2.json.JsonResource;
import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.Ingredient;
import com.sigmundgranaas.forgero.core.type.MutableTypeNode;
import com.sigmundgranaas.forgero.core.type.ResolvedTypeTree;
import com.sigmundgranaas.forgero.core.type.TypeTree;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sigmundgranaas.forgero.core.data.v2.json.JsonResourceType.TYPE_DEFINITION;
import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

@SuppressWarnings("ClassCanBeRecord")
public class ResourcePool {
    private final ImmutableList<JsonResource> resources;
    private final TypeTree tree;
    private List<JsonResource> resolvedConstructs;
    private List<JsonResource> unresolvedConstructs;
    private List<Composite> composites;

    public ResourcePool(ImmutableList<JsonResource> resources) {
        this.resources = resources;
        this.resolvedConstructs = new ArrayList<>();
        this.unresolvedConstructs = new ArrayList<>();

        this.composites = new ArrayList<>();
        this.tree = new TypeTree();
    }

    public ResolvedTypeTree createLoadedTypeTree() {
        new TypeFactory().convertJsonToData(resources).forEach(tree::addNode);
        tree.resolve();

        var resourceMap = resources.stream()
                .filter(resource -> Objects.nonNull(resource.name) && !resource.name.equals(EMPTY_IDENTIFIER))
                .filter(resource -> resource.resourceType != TYPE_DEFINITION)
                .collect(Collectors.groupingBy((entry -> entry.type), Collectors.mapping(resource -> resource, Collectors.toList())));


        resourceMap.forEach((entry, values) -> tree.find(entry).ifPresent(node -> values.stream().map(this::resourceToIngredient).flatMap(Optional::stream).forEach(node::addResource)));

        resources.stream().filter(resource -> resource.construct != null).forEach(unresolvedConstructs::add);

        int lastResolved = 0;
        int currentResolved = unresolvedConstructs.size();
        while (unresolvedConstructs.size() > 0) {
            lastResolved = unresolvedConstructs.size();
            var temporaryResolved = new ArrayList<JsonResource>();
            unresolvedConstructs.forEach(resource -> {
                assert resource.construct != null;
                assert resource.construct.recipe != null;
                var ingredients = resource.construct.recipe.ingredients;
                boolean resolved = true;
                var resourceIngredients = new ArrayList<Ingredient>();
                for (JsonIngredient ingredient : ingredients) {
                    int ingredientCount = resourceIngredients.size();
                    if (ingredient.id.equals("this")) {
                        resourceToIngredient(resource).ifPresent(resourceIngredients::add);
                    } else {
                        resourceIngredients.addAll(tree.find(ingredient.type)
                                .map(MutableTypeNode::ingredients)
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

            temporaryResolved.stream()
                    .map(this::constructToComposite)
                    .flatMap(List::stream)
                    .forEach(composites::add);

            temporaryResolved.stream()
                    .map(this::constructToComposite)
                    .flatMap(List::stream)
                    .map(Ingredient::of)
                    .forEach(ingredient -> tree.find(ingredient.type().typeName()).ifPresent(node -> node.addResource(ingredient)));

            for (JsonResource resource : temporaryResolved) {
                unresolvedConstructs.remove(resource);
                resolvedConstructs.add(resource);
            }

            resolvedConstructs.stream()
                    .map(this::constructToComposite)
                    .flatMap(List::stream)
                    .map(Ingredient::of)
                    .forEach(comp -> tree.find(comp.type().typeName()).ifPresent(node -> node.addResource(comp)));

            currentResolved = unresolvedConstructs.size();
        }

        resolvedConstructs.stream()
                .map(this::constructToComposite)
                .flatMap(List::stream)
                .forEach(composites::add);


        return tree.resolve();
    }

    private Optional<Ingredient> resourceToIngredient(JsonResource resource) {
        if (resource.property != null) {
            return Optional.of(Ingredient.of(resource.name, tree.type(resource.type), Collections.emptyList()));
        }
        return Optional.empty();
    }

    private List<Composite> constructToComposite(JsonResource resource) {
        var jsonIngredients = resource.construct.recipe.ingredients;
        var templateIngredients = new ArrayList<List<Ingredient>>();
        for (JsonIngredient ingredient : jsonIngredients) {
            if (ingredient.id != null && ingredient.id.equals("this")) {
                var thisIngredient = resourceToIngredient(resource).orElseThrow();
                templateIngredients.add(List.of(thisIngredient));
            } else if (ingredient.type != null) {
                if (ingredient.unique) {
                    var ingredients = tree.find(ingredient.type)
                            .map(MutableTypeNode::ingredients)
                            .map(List::stream)
                            .map(Stream::toList)
                            .orElse(Collections.emptyList());
                    templateIngredients.add(ingredients);
                } else {
                    var ingredients =
                            tree.find(ingredient.type)
                                    .map(MutableTypeNode::ingredients)
                                    .map(element -> element.stream().findFirst().map(List::of).orElse(Collections.emptyList()))
                                    .orElse(Collections.emptyList());

                    templateIngredients.add(ingredients);
                }
            }
        }

        var composites = new ArrayList<Composite>();
        for (int i = 0; i < templateIngredients.get(0).size(); i++) {
            for (int j = 0; j < templateIngredients.get(1).size(); j++) {
                var builder = Composite.builder();
                builder.add(templateIngredients.get(0).get(i));
                builder.type(tree.type(resource.construct.type));
                builder.add(templateIngredients.get(1).get(j));
                composites.add(builder.build());
            }
        }
        return composites;
    }
}
