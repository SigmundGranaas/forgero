package com.sigmundgranaas.forgero.resources;

import com.sigmundgranaas.forgero.client.forgerotool.model.ModelLayer;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.client.texture.FabricTextureIdentifierFactory;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.ToolPartModelTextureIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.texture.ForgeroToolPartTextureRegistry;
import com.sigmundgranaas.forgero.core.texture.palette.PaletteResourceRegistry;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RRPPreGenEntrypoint;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

import static com.sigmundgranaas.forgero.resources.ResourceLocations.MATERIAL_TEMPLATES_LOCATION;


public class ForgeroTextures implements RRPPreGenEntrypoint {
    public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create("forgero:resource");

    @Override
    public void pregen() {
        for (Path path : new FabricModFileLoader().getResourcesFromFolder(MATERIAL_TEMPLATES_LOCATION)) {
            PaletteResourceRegistry.getInstance().addPremadePalette(path.getFileName().toString().replace("_palette.png", ""));
        }
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            MaterialCollection materialCollection = ForgeroRegistry.getInstance().materialCollection();
            ForgeroToolPartTextureRegistry registry = ForgeroToolPartTextureRegistry.getInstance(new FabricTextureIdentifierFactory());
            for (ForgeroToolPart part : ForgeroRegistry.getInstance().toolPartCollection().getToolParts()) {

                registry.registerTexture(new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getName(), ToolPartModelType.getModelType(part), ModelLayer.PRIMARY, part.getSchematic().getModel()));
                if (part.getToolPartType() == ForgeroToolPartTypes.BINDING) {
                    registry.registerTexture(new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.PICKAXE), ModelLayer.PRIMARY, part.getSchematic().getModel()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.SHOVEL), ModelLayer.PRIMARY, part.getSchematic().getModel()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.AXE), ModelLayer.PRIMARY, part.getSchematic().getModel()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.HOE), ModelLayer.PRIMARY, part.getSchematic().getModel()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.SWORD), ModelLayer.PRIMARY, part.getSchematic().getModel()));
                }
                if (part.getToolPartType() == ForgeroToolPartTypes.HANDLE) {
                    registry.registerTexture(new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getName(), ToolPartModelType.MEDIUMHANDLE, ModelLayer.PRIMARY, part.getSchematic().getModel()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getName(), ToolPartModelType.FULLHANDLE, ModelLayer.PRIMARY, part.getSchematic().getModel()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getName(), ToolPartModelType.SHORTHANDLE, ModelLayer.PRIMARY, part.getSchematic().getModel()));
                }
            }

            for (SecondaryMaterial material : materialCollection.getMaterialsAsList().stream().filter(material -> material instanceof SecondaryMaterial).map(SecondaryMaterial.class::cast).toList()) {
                for (ForgeroToolPart part : ForgeroRegistry.getInstance().toolPartCollection().getToolParts()) {
                    registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part), ModelLayer.SECONDARY, part.getSchematic().getModel()));
                    if (part.getToolPartType() == ForgeroToolPartTypes.BINDING) {
                        registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.PICKAXE), ModelLayer.SECONDARY, part.getSchematic().getModel()));
                        registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.SHOVEL), ModelLayer.SECONDARY, part.getSchematic().getModel()));
                        registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.AXE), ModelLayer.SECONDARY, part.getSchematic().getModel()));
                        registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.SWORD), ModelLayer.SECONDARY, part.getSchematic().getModel()));
                        registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.HOE), ModelLayer.SECONDARY, part.getSchematic().getModel()));
                    }
                    if (part.getToolPartType() == ForgeroToolPartTypes.HANDLE) {
                        registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.MEDIUMHANDLE, ModelLayer.SECONDARY, part.getSchematic().getModel()));
                        registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.FULLHANDLE, ModelLayer.SECONDARY, part.getSchematic().getModel()));
                        registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.SHORTHANDLE, ModelLayer.SECONDARY, part.getSchematic().getModel()));
                    }
                }
                for (Gem gem : ForgeroRegistry.getInstance().gemCollection().getGems()) {
                    for (ForgeroToolPart part : ForgeroRegistry.getInstance().toolPartCollection().getToolParts()) {

                        if (part instanceof ToolPartHead head) {
                            registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.getModelType(head, head.getToolType()), ModelLayer.GEM, part.getSchematic().getModel()));
                        }
                        if (part.getToolPartType() == ForgeroToolPartTypes.BINDING) {
                            registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.BINDING, ModelLayer.GEM, part.getSchematic().getModel()));
                            registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.PICKAXEBINDING, ModelLayer.GEM, part.getSchematic().getModel()));
                            registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.SHOVELBINDING, ModelLayer.GEM, part.getSchematic().getModel()));
                            registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.AXEHEADBINDING, ModelLayer.GEM, part.getSchematic().getModel()));
                            registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.HOEBINDING, ModelLayer.GEM, part.getSchematic().getModel()));
                            registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.SWORDBINDING, ModelLayer.GEM, part.getSchematic().getModel()));
                        }
                        if (part.getToolPartType() == ForgeroToolPartTypes.HANDLE) {
                            registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.HANDLE, ModelLayer.GEM, part.getSchematic().getModel()));
                            registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.SHORTHANDLE, ModelLayer.GEM, part.getSchematic().getModel()));
                            registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.MEDIUMHANDLE, ModelLayer.GEM, part.getSchematic().getModel()));
                            registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.FULLHANDLE, ModelLayer.GEM, part.getSchematic().getModel()));
                        }
                    }
                }
            }

            //registry.getTextures().stream().filter(id -> PaletteResourceRegistry.getInstance().premadePalette(id.getPaletteIdentifier())).forEach(texture -> RESOURCE_PACK.addTexture(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "item/" + texture.getIdentifier()), CachedToolPartTextureService.getInstance(new FabricTextureLoader((textureId) -> MinecraftClient.getInstance().getResourceManager().getResource(textureId))).getTexture(texture).getImage()));

        }

        RRPCallback.BEFORE_VANILLA.register(a -> a.add(RESOURCE_PACK));
    }
}
