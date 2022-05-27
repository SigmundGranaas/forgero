package com.sigmundgranaas.forgero.resources;

import com.sigmundgranaas.forgero.client.forgerotool.model.ModelLayer;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.client.texture.FabricTextureIdentifierFactory;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.ToolPartModelTextureIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.schematic.HeadSchematic;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.texture.ForgeroToolPartTextureRegistry;
import com.sigmundgranaas.forgero.core.texture.palette.PaletteResourceRegistry;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RRPPreGenEntrypoint;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

import static com.sigmundgranaas.forgero.resources.ResourceLocations.MATERIAL_TEMPLATES_LOCATION;


public class ForgeroTextures implements RRPPreGenEntrypoint {
    public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create("forgero:textures");

    @Override
    public void pregen() {
        for (Path path : new FabricModFileLoader().getResourcesFromFolder(MATERIAL_TEMPLATES_LOCATION)) {
            PaletteResourceRegistry.getInstance().addPremadePalette(path.getFileName().toString().replace("_palette.png", ""));
        }
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            MaterialCollection materialCollection = ForgeroRegistry.getInstance().materialCollection();
            ForgeroToolPartTextureRegistry registry = ForgeroToolPartTextureRegistry.getInstance(new FabricTextureIdentifierFactory());

            //registry.getTextures().stream().filter(id -> PaletteResourceRegistry.getInstance().premadePalette(id.getPaletteIdentifier())).forEach(texture -> RESOURCE_PACK.addTexture(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "item/" + texture.getIdentifier()), CachedToolPartTextureService.getInstance(new FabricTextureLoader((textureId) -> MinecraftClient.getInstance().getResourceManager().getResource(textureId))).getTexture(texture).getImage()));

            registerToolPartModelsPrimary(registry);
            registerToolPartModelsSecondary(registry);
            registerToolPartGemModels(registry);

            RRPCallback.AFTER_VANILLA.register(a -> a.add(RESOURCE_PACK));
        }
    }

    private void registerToolPartModelsPrimary(ForgeroToolPartTextureRegistry registry) {
        for (Schematic schematic : ForgeroRegistry.getInstance().schematicCollection().getSchematics()) {
            for (PrimaryMaterial material : ForgeroRegistry.getInstance().materialCollection().getPrimaryMaterialsAsList()) {
                registerModel(registry, schematic, material, ModelLayer.PRIMARY);
            }
        }
    }

    private void registerModel(ForgeroToolPartTextureRegistry registry, Schematic schematic, ForgeroMaterial material, ModelLayer layer) {
        registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(schematic), layer, schematic.getModel()));
        if (schematic.getType() == ForgeroToolPartTypes.BINDING) {
            for (ForgeroToolTypes type : ForgeroToolTypes.values()) {
                registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(type, schematic.getType()), layer, schematic.getModel()));
            }
        }
        if (schematic.getType() == ForgeroToolPartTypes.HANDLE) {
            registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.MEDIUMHANDLE, layer, schematic.getModel()));
            registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.FULLHANDLE, layer, schematic.getModel()));
            registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.SHORTHANDLE, layer, schematic.getModel()));
        }
    }

    private void registerToolPartModelsSecondary(ForgeroToolPartTextureRegistry registry) {
        for (Schematic schematic : ForgeroRegistry.getInstance().schematicCollection().getSchematics()) {
            for (SecondaryMaterial material : ForgeroRegistry.getInstance().materialCollection().getSecondaryMaterialsAsList()) {
                registerModel(registry, schematic, material, ModelLayer.SECONDARY);
            }
        }
    }

    private void registerToolPartGemModels(ForgeroToolPartTextureRegistry registry) {
        for (Schematic schematic : ForgeroRegistry.getInstance().schematicCollection().getSchematics()) {
            for (Gem gem : ForgeroRegistry.getInstance().gemCollection().getGems()) {
                if (schematic instanceof HeadSchematic head) {
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.getModelType(head.getToolType(), schematic.getType()), ModelLayer.GEM, schematic.getModel()));
                }
                if (schematic.getType() == ForgeroToolPartTypes.BINDING) {
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.BINDING, ModelLayer.GEM, schematic.getModel()));
                    for (ForgeroToolTypes type : ForgeroToolTypes.values()) {
                        registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.getModelType(type, schematic.getType()), ModelLayer.GEM, schematic.getModel()));
                    }

                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.PICKAXEBINDING, ModelLayer.GEM, schematic.getModel()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.SHOVELBINDING, ModelLayer.GEM, schematic.getModel()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.AXEHEADBINDING, ModelLayer.GEM, schematic.getModel()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.HOEBINDING, ModelLayer.GEM, schematic.getModel()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.SWORDBINDING, ModelLayer.GEM, schematic.getModel()));
                }
                if (schematic.getType() == ForgeroToolPartTypes.HANDLE) {
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.HANDLE, ModelLayer.GEM, schematic.getModel()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.SHORTHANDLE, ModelLayer.GEM, schematic.getModel()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.MEDIUMHANDLE, ModelLayer.GEM, schematic.getModel()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.FULLHANDLE, ModelLayer.GEM, schematic.getModel()));
                }

            }
        }

    }

}