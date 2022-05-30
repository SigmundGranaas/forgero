package com.sigmundgranaas.forgero.resources.external;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeHandlersEvent;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KubeJsIntegration extends KubeJSPlugin {

    @Override
    public void addRecipes(RegisterRecipeHandlersEvent event) {
        super.addRecipes(event);
        var materials = List.of("wooden", "stone", "iron", "golden", "diamond", "netherite");
        var tools = List.of("pickaxe", "shovel", "hoe", "axe", "sword");

        Set<String> vanillaTools = materials.stream().map(material -> tools.stream().map(tool -> String.format("minecraft:%s_%s", material, tool)).toList()).flatMap(List::stream).collect(Collectors.toSet());

        var removals = event.map()
                .values().stream()
                .filter(recipe ->
                        recipe.factory.get().inputItems.stream()
                                .filter(ingredient -> ingredient.getItemIds().size() == 1)
                                .anyMatch(ingredient -> !Collections.disjoint(vanillaTools, ingredient.getItemIds())))
                .map(recipe -> recipe.factory.get().getId());

        removals.forEach(event::ignore);
    }
}
