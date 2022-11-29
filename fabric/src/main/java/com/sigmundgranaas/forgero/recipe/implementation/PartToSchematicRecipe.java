package com.sigmundgranaas.forgero.recipe.implementation;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.state.Composite;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Consumer;

public class PartToSchematicRecipe extends FabricRecipeProvider {
    public PartToSchematicRecipe(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateRecipes(Consumer<RecipeJsonProvider> exporter) {
        ForgeroStateRegistry.STATES.all().stream()
                .filter(Composite.class::isInstance)
                .map(Composite.class::cast)
                .filter(comp -> comp.ingredients().stream().anyMatch(ingredient -> ingredient.name().contains("schematic")))
                .forEach(comp -> createSchematicRecipeFromComposite(comp, exporter));
        // ShapedRecipeJsonBuilder.
    }

    private void createSchematicRecipeFromComposite(Composite composite, Consumer<RecipeJsonProvider> exporter) {
        var schematic = composite.ingredients().stream().filter(ingredient -> ingredient.name().contains("schematic")).findFirst();
        if (schematic.isPresent()) {
            var recipe = ShapelessRecipeJsonBuilder.create(Registry.ITEM.get(new Identifier(schematic.get().identifier())));
            recipe.input(Registry.ITEM.get(new Identifier(composite.identifier())));
            recipe.input(Items.PAPER);
            var id = new Identifier(composite.nameSpace(), composite.name() + "-schematic_recipe");
            recipe.criterion("okay", RecipeUnlockedCriterion.create(id));

            recipe.offerTo(exporter, id);
        }
    }
}
