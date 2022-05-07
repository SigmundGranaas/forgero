package com.sigmundgranaas.forgero.core;

import com.sigmundgranaas.forgero.core.gem.GemCollection;
import com.sigmundgranaas.forgero.core.gem.implementation.FileGemLoader;
import com.sigmundgranaas.forgero.core.gem.implementation.GemCollectionImpl;
import com.sigmundgranaas.forgero.core.gem.implementation.GemFactory;
import com.sigmundgranaas.forgero.core.gem.implementation.GemPOJO;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.implementation.MaterialCollectionImpl;
import com.sigmundgranaas.forgero.core.material.implementation.SimpleMaterialLoader;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.PaletteResourceIdentifier;
import com.sigmundgranaas.forgero.core.material.material.ResourceIdentifier;
import com.sigmundgranaas.forgero.core.material.material.factory.MaterialFactory;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleMaterialPOJO;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.schematic.SchematicCollection;
import com.sigmundgranaas.forgero.core.schematic.SchematicLoader;
import com.sigmundgranaas.forgero.core.schematic.SchematicPOJO;
import com.sigmundgranaas.forgero.core.texture.palette.PaletteResourceRegistry;
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
import com.sigmundgranaas.forgero.resources.FabricModPOJOLoader;
import com.sigmundgranaas.forgero.resources.ResourceLocations;

import java.util.*;
import java.util.stream.Collectors;

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
        //registerDefaultMaterials();
        //registerDefaultGems();

    }

    public ForgeroRegistry initializeForgeroResources() {
        MaterialCollection materialCollection = initializeMaterials();
        GemCollection gemCollection = initializeGems();
        SchematicCollection schematicCollection = initializeSchematicCollection();
        ForgeroToolPartCollection toolPartCollection = initializeToolParts(materialCollection, schematicCollection);
        ForgeroToolCollection toolCollection = initializeToolCollection(toolPartCollection);
        return ForgeroRegistry.initializeRegistry(materialCollection, gemCollection, toolCollection, toolPartCollection, schematicCollection);
    }

    private SchematicCollection initializeSchematicCollection() {
        var pojos = new FabricModPOJOLoader<>(SchematicPOJO.class, ResourceLocations.SCHEMATIC_LOCATION).loadPojosFromMods();
        List<Schematic> schematics = pojos.stream().map(SchematicPOJO::createSchematicFromPojo).toList();

        if (schematics.isEmpty()) {
            schematics = new SchematicLoader().loadSchematics();
        }
        return new SchematicCollection(schematics);
    }


    private ForgeroToolCollection initializeToolCollection(ForgeroToolPartCollection toolPartCollection) {
        ForgeroToolFactory factory = new ForgeroToolFactoryImpl();
        return new ForgeroToolCollectionImpl(factory.createForgeroTools(toolPartCollection));
    }

    private ForgeroToolPartCollection initializeToolParts(MaterialCollection materialCollection, SchematicCollection collection) {
        ForgeroToolPartFactory factory = new ForgeroToolPartFactoryImpl();
        return new ForgeroToolPartCollectionImpl(factory.createBaseToolParts(materialCollection, collection));
    }

    private GemCollection initializeGems() {
        var pojos = new FabricModPOJOLoader<>(GemPOJO.class, ResourceLocations.GEM_LOCATION).loadPojosFromMods();

        pojos.forEach(pojo -> {
            List<ResourceIdentifier> inclusions = pojo.palette.include.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
            List<ResourceIdentifier> exclusions = pojo.palette.exclude.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
            PaletteResourceRegistry.getInstance().addPalette(new PaletteResourceIdentifier(pojo.palette.name, inclusions, exclusions));
        });
        GemFactory factory = new GemFactory();
        var gems = pojos.stream().map(factory::createGem).collect(Collectors.toList());

        if (pojos.isEmpty()) {
            gems = new FileGemLoader(List.of("diamond", "emerald", "lapis")).loadGems();
        }
        return new GemCollectionImpl(gems);
    }

    private MaterialCollection initializeMaterials() {
        Map<String, ForgeroMaterial> materials = new HashMap<>();
        var pojos = new FabricModPOJOLoader<>(SimpleMaterialPOJO.class, ResourceLocations.MATERIAL_LOCATION).loadPojosFromMods();

        pojos.forEach(pojo -> {
            List<ResourceIdentifier> inclusions = pojo.palette.include.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
            List<ResourceIdentifier> exclusions = pojo.palette.exclude.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
            PaletteResourceRegistry.getInstance().addPalette(new PaletteResourceIdentifier(pojo.palette.name, inclusions, exclusions));
        });
        pojos.stream().sorted(Comparator.comparingInt(material -> material.rarity)).forEach(material -> materials.put(material.name.toLowerCase(Locale.ROOT), MaterialFactory.INSTANCE.createMaterial(material)));


        if (materials.isEmpty()) {
            return new MaterialCollectionImpl(new SimpleMaterialLoader(List.of("iron", "oak", "diamond", "netherite")).getMaterials());

        }

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
