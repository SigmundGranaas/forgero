package com.sigmundgranaas.forgero.core.resource.data.processor;

import com.sigmundgranaas.forgero.core.identifier.Common;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.*;
import com.sigmundgranaas.forgero.core.util.Identifiers;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;
import static com.sigmundgranaas.forgero.core.util.Identifiers.THIS_IDENTIFIER;

public class RecipeInflater {
    private final DataResource resource;

    private final Function<String, List<DataResource>> typeFinder;

    private final Function<String, Optional<DataResource>> idFinder;

    private final Function<String, Optional<DataResource>> templateProvider;

    public RecipeInflater(DataResource resource, Function<String, List<DataResource>> typeFinder, Function<String, Optional<DataResource>> idFinder, Function<String, Optional<DataResource>> templateProvider) {
        this.resource = resource;
        this.typeFinder = typeFinder;
        this.idFinder = idFinder;
        this.templateProvider = templateProvider;
    }

    public List<RecipeData> process() {
        if (invalidData()) {
            return Collections.emptyList();
        }
        var templateIngredients = resource.construct()
                .flatMap(ConstructData::recipes)
                .orElse(Collections.emptyList())
                .stream()
                .map(this::inflateIngredients)
                .flatMap(List::stream)
                .toList();

        return Collections.emptyList();
    }

    private List<List<IngredientData>> inflateIngredients(RecipeData recipe) {
        var recipes = new ArrayList<>();
        var templateIngredients = new ArrayList<List<IngredientData>>();
        for (IngredientData ingredient : recipe.ingredients()) {
            if (ingredient.id().equals(Identifiers.THIS_IDENTIFIER)) {
                templateIngredients.add(List.of(IngredientData.builder().id(resource.identifier()).unique(true).build()));
            } else if (!ingredient.type().equals(Identifiers.EMPTY_IDENTIFIER)) {
                if (ingredient.unique()) {
                    templateIngredients.add(findUniqueIngredients(ingredient.type()));
                } else {
                    templateIngredients.add(findDefaultIngredients(ingredient.type()));
                }
            }
        }

        for (int i = 0; i < templateIngredients.size(); i++) {
            for (int j = 0; j < templateIngredients.get(i).size(); j++) {
                var builder = recipe.toBuilder();
                var ingredients = new ArrayList<IngredientData>();
                IngredientData currentIngredient = templateIngredients.get(i).get(j);
                ingredients.add(currentIngredient);
                HashMap<List<Integer>, List<IngredientData>> mappedValues = new HashMap<>();
                for (int x = 0; x < templateIngredients.size(); x++) {
                    if (x == i) {
                        break;
                    }
                    for (int p = 0; p < templateIngredients.get(x).size(); p++) {
                        if (mappedValues.containsKey()) {

                        }
                        ingredients.add(templateIngredients.get(x).get(p));
                    }
                }
                builder.ingredients(ingredients);
                recipes.add(builder.build());
            }
        }
        return templateIngredients;
    }


    private List<RecipeData> buildRecipes(List<List<IngredientData>> templateIngredients) {
        var recipes = new ArrayList<RecipeData>();
        for (int i = 0; i < templateIngredients.get(0).size(); i++) {
            for (int j = 0; j < templateIngredients.get(1).size(); j++) {
                var newComponents = new ArrayList<IngredientData>();
                newComponents.add(templateIngredients.get(0).get(i));
                newComponents.add(templateIngredients.get(1).get(j));
                String name = String.join(Common.ELEMENT_SEPARATOR, newComponents.stream().map(IngredientData::id).map(this::idToName).toList());
                recipes.add(RecipeData.builder()
                        .ingredients(newComponents)
                        .craftingType("data.type()")
                        .target(resource.nameSpace() + ":" + name)
                        .build());
            }
        }
        return recipes;
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
