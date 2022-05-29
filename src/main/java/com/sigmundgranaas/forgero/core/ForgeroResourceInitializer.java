package com.sigmundgranaas.forgero.core;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.data.factory.GemFactory;
import com.sigmundgranaas.forgero.core.data.factory.MaterialFactory;
import com.sigmundgranaas.forgero.core.data.factory.SchematicFactory;
import com.sigmundgranaas.forgero.core.data.v1.pojo.GemPOJO;
import com.sigmundgranaas.forgero.core.data.v1.pojo.MaterialPOJO;
import com.sigmundgranaas.forgero.core.data.v1.pojo.SchematicPOJO;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.implementation.FileGemLoader;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.material.implementation.SimpleMaterialLoader;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.PaletteResourceIdentifier;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.ResourceIdentifier;
import com.sigmundgranaas.forgero.core.registry.ForgeroResources;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.schematic.SchematicLoader;
import com.sigmundgranaas.forgero.core.texture.palette.PaletteResourceRegistry;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactoryImpl;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactoryImpl;
import com.sigmundgranaas.forgero.core.util.JsonPOJOLoader;
import com.sigmundgranaas.forgero.core.util.ListPOJO;
import com.sigmundgranaas.forgero.resources.FabricModPOJOLoader;
import com.sigmundgranaas.forgero.resources.ForgeroResourceModContainerService;
import com.sigmundgranaas.forgero.resources.ResourceLocations;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Deprecated resource initializer. Will be reworked
 * <p>
 * Loads all resources from configuration files picked up from support Mod Containers
 */
public class ForgeroResourceInitializer {
    private final List<String> registeredMaterials;
    private final List<String> materialsExclusions;
    private final List<String> registeredGems;
    private final List<String> gemExclusions;

    private final Set<String> availableDependencies;

    public ForgeroResourceInitializer() {
        this.materialsExclusions = new ArrayList<>();
        this.gemExclusions = new ArrayList<>();
        this.registeredMaterials = new ArrayList<>();
        this.registeredGems = new ArrayList<>();
        this.availableDependencies = new ForgeroResourceModContainerService().getAllModsAsSet();
    }

    public void registerDefaultResources() {
        //registerDefaultMaterials();
        //registerDefaultGems();

    }

    public ForgeroResources initializeForgeroResources() {
        var materialCollection = initializeMaterials();
        var gemCollection = initializeGems();
        var schematicCollection = initializeSchematicCollection();
        var toolPartCollection = initializeToolParts(materialCollection, schematicCollection);
        var toolCollection = initializeToolCollection(toolPartCollection);
        return new ForgeroResources(materialCollection, gemCollection, toolCollection, toolPartCollection, schematicCollection);
    }

    private List<Schematic> initializeSchematicCollection() {
        var pojos = new FabricModPOJOLoader<>(SchematicPOJO.class, ResourceLocations.SCHEMATIC_LOCATION).loadPojosFromMods();
        var factory = new SchematicFactory(pojos, Set.of("forgero", "minecraft"));
        List<Schematic> schematics = pojos.stream().map(factory::buildResource).flatMap(Optional::stream).toList();

        if (schematics.isEmpty()) {
            schematics = new SchematicLoader().loadSchematics();
        }
        return schematics;
    }


    private List<ForgeroTool> initializeToolCollection(List<ForgeroToolPart> toolPartCollection) {
        ForgeroToolFactory factory = new ForgeroToolFactoryImpl();
        return factory.createForgeroTools(toolPartCollection);
    }

    private List<ForgeroToolPart> initializeToolParts(Map<String, ForgeroMaterial> materialCollection, List<Schematic> collection) {
        ForgeroToolPartFactory factory = new ForgeroToolPartFactoryImpl();
        return factory.createBaseToolParts(materialCollection.values().stream().filter(material -> material instanceof PrimaryMaterial).map(PrimaryMaterial.class::cast).toList(), collection);
    }

    private List<Gem> initializeGems() {
        var pojos = new FabricModPOJOLoader<>(GemPOJO.class, ResourceLocations.GEM_LOCATION).loadPojosFromMods();

        pojos.forEach(pojo -> {

            List<ResourceIdentifier> inclusions = pojo.palette.include.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
            List<ResourceIdentifier> exclusions = pojo.palette.exclude.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
            PaletteResourceRegistry.getInstance().addPalette(new PaletteResourceIdentifier(pojo.palette.name, inclusions, exclusions));
        });
        GemFactory factory = new GemFactory(pojos, this.availableDependencies);
        var gems = pojos.stream().map(factory::buildResource).flatMap(Optional::stream).collect(Collectors.toList());

        if (pojos.isEmpty()) {
            gems = new FileGemLoader(List.of("diamond", "emerald", "lapis")).loadGems();
        }
        return gems;
    }

    private Map<String, ForgeroMaterial> initializeMaterials() {
        Map<String, ForgeroMaterial> materials;
        var pojos = new FabricModPOJOLoader<>(MaterialPOJO.class, ResourceLocations.MATERIAL_LOCATION).loadPojosFromMods();
        try {
            pojos.forEach(pojo -> {
                if (!pojo.abstractResource) {
                    List<ResourceIdentifier> inclusions = pojo.palette.include.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
                    List<ResourceIdentifier> exclusions = pojo.palette.exclude.stream().map(paletteIdentifiers -> new ResourceIdentifier(new PaletteIdentifier(pojo.palette.name), paletteIdentifiers)).collect(Collectors.toList());
                    PaletteResourceRegistry.getInstance().addPalette(new PaletteResourceIdentifier(pojo.palette.name, inclusions, exclusions));
                }
            });
        } catch (NullPointerException e) {
            ForgeroInitializer.LOGGER.error("Error occurred trying to load materials. Likely due to Malformed JSON");
            ForgeroInitializer.LOGGER.error(e);
        }
        var factory = MaterialFactory.createFactory(pojos, this.availableDependencies);

        materials = pojos.stream()
                .map(factory::buildResource)
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(ForgeroMaterial::getResourceName, material -> material));

        if (materials.isEmpty()) {
            return new SimpleMaterialLoader(List.of("iron", "oak", "diamond", "netherite")).getMaterials();
        }

        return materials;
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
