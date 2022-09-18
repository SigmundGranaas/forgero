package com.sigmundgranaas.forgero.resources;

import com.sigmundgranaas.forgero.deprecated.ModelLayer;
import com.sigmundgranaas.forgero.deprecated.ToolPartModelType;
import com.sigmundgranaas.forgero.client.texture.FabricTextureIdentifierFactory;
import com.sigmundgranaas.forgero.ForgeroRegistry;
import com.sigmundgranaas.forgero.gem.Gem;
import com.sigmundgranaas.forgero.identifier.texture.toolpart.ToolPartModelTextureIdentifier;
import com.sigmundgranaas.forgero.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.schematic.Schematic;
import com.sigmundgranaas.forgero.texture.ForgeroToolPartTextureRegistry;
import com.sigmundgranaas.forgero.texture.TextureModel;
import com.sigmundgranaas.forgero.texture.TextureModelContainer;
import com.sigmundgranaas.forgero.texture.palette.PaletteResourceRegistry;
import com.sigmundgranaas.forgero.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.resources.loader.FabricResourceLoader;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.Collection;

import static com.sigmundgranaas.forgero.deprecated.ToolPartModelType.*;
import static com.sigmundgranaas.forgero.resources.loader.ResourceLocations.MATERIAL_TEMPLATES_LOCATION;


public class ForgeroTextures {


    public void createTextureIdentifiers() {
        for (Path path : new FabricModFileLoader().getResourcesFromFolder(MATERIAL_TEMPLATES_LOCATION)) {
            PaletteResourceRegistry.getInstance().addPremadePalette(path.getFileName().toString().replace(".png", ""));
        }
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            //var materialCollection = ForgeroRegistry.MATERIAL.list();
            ForgeroToolPartTextureRegistry registry = ForgeroToolPartTextureRegistry.getInstance(new FabricTextureIdentifierFactory());

            var loader = new FabricResourceLoader(new ModContainerService().getAllModsAsSet());
            ForgeroRegistry.INSTANCE.loadResourcesIfEmpty(loader);


            registerToolPartModelsPrimary(registry);
            registerToolPartModelsSecondary(registry);
            registerToolPartGemModels(registry);
            registerSchematicModels(registry);

        }
    }

    private void registerSchematicModels(ForgeroToolPartTextureRegistry registry) {
        for (TextureModel model : ForgeroRegistry.SCHEMATIC.stream().map(Schematic::getModelContainer).map(TextureModelContainer::getModels).flatMap(Collection::stream).toList()) {
            for (PrimaryMaterial material : ForgeroRegistry.MATERIAL.getPrimaryMaterials()) {
                // registry.registerTexture(model.);
            }
        }
    }

    private void registerToolPartModelsPrimary(ForgeroToolPartTextureRegistry registry) {
        for (Schematic schematic : ForgeroRegistry.SCHEMATIC.list()) {
            for (PrimaryMaterial material : ForgeroRegistry.MATERIAL.getPrimaryMaterials()) {
                registerModel(registry, schematic, material, ModelLayer.PRIMARY);
            }
        }
    }

    private void registerToolPartModelsSecondary(ForgeroToolPartTextureRegistry registry) {
        for (Schematic schematic : ForgeroRegistry.SCHEMATIC.list()) {
            for (SecondaryMaterial material : ForgeroRegistry.MATERIAL.getSecondaryMaterials()) {
                registerModel(registry, schematic, material, ModelLayer.SECONDARY);
            }
        }
    }

    private void registerModel(ForgeroToolPartTextureRegistry registry, Schematic schematic, ForgeroMaterial material, ModelLayer layer) {
        registry.registerTexture(new ToolPartModelTextureIdentifier(material.getResourceName(), ToolPartModelType.getModelType(schematic), layer, schematic.getResourceName()));
        if (schematic.getType() == ForgeroToolPartTypes.BINDING) {
            for (ForgeroToolTypes type : ForgeroToolTypes.values()) {
                registry.registerTexture(new ToolPartModelTextureIdentifier(material.getResourceName(), ToolPartModelType.getModelType(type, schematic.getType()), layer, schematic.getResourceName()));
            }
        }
        if (schematic.getType() == ForgeroToolPartTypes.HANDLE) {
            registry.registerTexture(new ToolPartModelTextureIdentifier(material.getResourceName(), ToolPartModelType.MEDIUM_HANDLE, layer, schematic.getResourceName()));
            registry.registerTexture(new ToolPartModelTextureIdentifier(material.getResourceName(), ToolPartModelType.FULL_HANDLE, layer, schematic.getResourceName()));
            registry.registerTexture(new ToolPartModelTextureIdentifier(material.getResourceName(), ToolPartModelType.SHORT_HANDLE, layer, schematic.getResourceName()));
        }
    }

    private void registerToolPartGemModels(ForgeroToolPartTextureRegistry registry) {
        for (Schematic schematic : ForgeroRegistry.SCHEMATIC.list()) {
            for (Gem gem : ForgeroRegistry.GEM.list()) {
                if (schematic instanceof HeadSchematic head) {
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getResourceName(), ToolPartModelType.getModelType(head.getToolType(), schematic.getType()), ModelLayer.GEM, schematic.getResourceName()));
                }
                if (schematic.getType() == ForgeroToolPartTypes.BINDING) {
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getResourceName(), ToolPartModelType.BINDING, ModelLayer.GEM, schematic.getResourceName()));
                    for (ForgeroToolTypes type : ForgeroToolTypes.values()) {
                        registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getResourceName(), ToolPartModelType.getModelType(type, schematic.getType()), ModelLayer.GEM, schematic.getResourceName()));
                    }

                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getResourceName(), PICKAXE_BINDING, ModelLayer.GEM, schematic.getResourceName()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getResourceName(), SHOVEL_BINDING, ModelLayer.GEM, schematic.getResourceName()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getResourceName(), AXE_BINDING, ModelLayer.GEM, schematic.getResourceName()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getResourceName(), HOE_BINDING, ModelLayer.GEM, schematic.getResourceName()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getResourceName(), SWORD_BINDING, ModelLayer.GEM, schematic.getResourceName()));
                }
                if (schematic.getType() == ForgeroToolPartTypes.HANDLE) {
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getResourceName(), HANDLE, ModelLayer.GEM, schematic.getResourceName()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getResourceName(), SHORT_HANDLE, ModelLayer.GEM, schematic.getResourceName()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getResourceName(), MEDIUM_HANDLE, ModelLayer.GEM, schematic.getResourceName()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getResourceName(), FULL_HANDLE, ModelLayer.GEM, schematic.getResourceName()));
                }

            }
        }

    }

}