package com.sigmundgranaas.forgero.core;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.GemCollection;
import com.sigmundgranaas.forgero.core.gem.implementation.FileGemLoader;
import com.sigmundgranaas.forgero.core.gem.implementation.GemCollectionImpl;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.implementation.MaterialCollectionImpl;
import com.sigmundgranaas.forgero.core.material.implementation.SimpleMaterialLoader;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.pattern.Pattern;
import com.sigmundgranaas.forgero.core.pattern.PatternCollection;
import com.sigmundgranaas.forgero.core.pattern.PatternLoader;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolCollection;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolCollectionImpl;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactoryImpl;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartCollection;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartCollectionImpl;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactoryImpl;
import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;
import com.sigmundgranaas.forgero.core.util.ListPOJO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ForgeroResourceInitializer {
    private final List<String> registeredMaterials;
    private final List<String> materialsExclusions;
    private final List<String> registeredGems;
    private final List<String> gemExclusions;

    public ForgeroResourceInitializer() {
        this.materialsExclusions = new ArrayList<>();
        this.gemExclusions = new ArrayList<>();
        this.registeredMaterials = new ArrayList<>();
        this.registeredGems = new ArrayList<>();
    }

    public void registerDefaultResources() {
        registerDefaultMaterials();
        registerDefaultGems();
    }

    public ForgeroRegistry initializeForgeroResources() {
        MaterialCollection materialCollection = initializeMaterials();
        GemCollection gemCollection = initializeGems();
        PatternCollection patternCollection = initializePatternCollection();
        ForgeroToolPartCollection toolPartCollection = initializeToolParts(materialCollection, patternCollection);
        ForgeroToolCollection toolCollection = initializeToolCollection(toolPartCollection);
        return ForgeroRegistry.initializeRegistry(materialCollection, gemCollection, toolCollection, toolPartCollection, patternCollection);
    }

    private PatternCollection initializePatternCollection() {
        List<Pattern> patterns = new PatternLoader().loadPatterns();
        return new PatternCollection(patterns);
    }

    private ForgeroToolCollection initializeToolCollection(ForgeroToolPartCollection toolPartCollection) {
        ForgeroToolFactory factory = new ForgeroToolFactoryImpl();
        return new ForgeroToolCollectionImpl(factory.createForgeroTools(toolPartCollection));
    }

    private ForgeroToolPartCollection initializeToolParts(MaterialCollection materialCollection, PatternCollection collection) {
        ForgeroToolPartFactory factory = new ForgeroToolPartFactoryImpl();
        return new ForgeroToolPartCollectionImpl(factory.createBaseToolParts(materialCollection, collection));
    }

    private GemCollection initializeGems() {
        List<Gem> gems = new FileGemLoader(registeredGems).loadGems();
        return new GemCollectionImpl(gems);
    }

    private MaterialCollection initializeMaterials() {
        Map<String, ForgeroMaterial> materials = new SimpleMaterialLoader(registeredMaterials).getMaterials();
        return new MaterialCollectionImpl(materials);
    }

    public boolean excludeGem(String gem) {
        return gemExclusions.add(gem);
    }

    public boolean excludeMaterial(String materials) {
        return materialsExclusions.add(materials);
    }


    public void registerDefaultMaterials() {
        JsonPOJOLoader
                .loadPOJO("/data/forgero/materials/materials.json", ListPOJO.class)
                .ifPresent(materialList -> registeredMaterials.addAll(materialList.elements));
    }

    public void registerDefaultGems() {
        JsonPOJOLoader
                .loadPOJO("/data/forgero/gems/gems.json", ListPOJO.class)
                .ifPresent(gemList -> registeredGems.addAll(gemList.elements));
    }
}
