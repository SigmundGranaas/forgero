package com.sigmundgranaas.forgero.datagen;

import com.sigmundgranaas.forgero.recipe.implementation.PartToSchematicRecipe;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class ForgeroDataGeneration implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        fabricDataGenerator.addProvider(PartToSchematicRecipe::new);
    }
}
