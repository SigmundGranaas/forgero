package com.sigmundgranaas.forgero.client;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.client.forgerotool.model.ForgeroModelVariantProvider;
import com.sigmundgranaas.forgero.client.forgerotool.model.ModelLayer;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.client.forgerotool.model.UnbakedModelCollection;
import com.sigmundgranaas.forgero.client.texture.FabricTextureIdentifierFactory;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.ToolPartModelTextureIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.texture.ForgeroToolPartTextureRegistry;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.item.ItemCollection;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ForgeroClient implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger(ForgeroInitializer.MOD_NAMESPACE);
    public static Set<ModelIdentifier> modelSet = new HashSet<>();

    @Override
    public void onInitializeClient() {
        initializeItemModels();
    }

    private void initializeItemModels() {
        registerToolPartModels();
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(variant -> new ForgeroModelVariantProvider(UnbakedModelCollection.INSTANCE));
    }

    private void registerToolPartModels() {
        ItemCollection itemCollection = ItemCollection.INSTANCE;
        MaterialCollection materialCollection = ForgeroRegistry.getInstance().materialCollection();

        ForgeroToolPartTextureRegistry registry = ForgeroToolPartTextureRegistry.getInstance(new FabricTextureIdentifierFactory());


        for (Item toolPart : itemCollection.getToolParts()) {
            ForgeroToolPart part = ((ToolPartItem) toolPart).getPart();
            registry.registerTexture(new ToolPartModelTextureIdentifier(((ToolPartItem) toolPart).getPrimaryMaterial().getName(), ToolPartModelType.getModelType(part), ModelLayer.PRIMARY, part.getPattern().getVariant()));
            if (part.getToolPartType() == ForgeroToolPartTypes.BINDING) {
                registry.registerTexture(new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.PICKAXE), ModelLayer.PRIMARY, part.getPattern().getVariant()));
                registry.registerTexture(new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.SHOVEL), ModelLayer.PRIMARY, part.getPattern().getVariant()));
                registry.registerTexture(new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.AXE), ModelLayer.PRIMARY, part.getPattern().getVariant()));
            }
            if (part.getToolPartType() == ForgeroToolPartTypes.HANDLE) {
                registry.registerTexture(new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getName(), ToolPartModelType.MEDIUMHANDLE, ModelLayer.PRIMARY, part.getPattern().getVariant()));
                registry.registerTexture(new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getName(), ToolPartModelType.FULLHANDLE, ModelLayer.PRIMARY, part.getPattern().getVariant()));
                registry.registerTexture(new ToolPartModelTextureIdentifier(part.getPrimaryMaterial().getName(), ToolPartModelType.SHORTHANDLE, ModelLayer.PRIMARY, part.getPattern().getVariant()));
            }
        }


        for (SecondaryMaterial material : materialCollection.getMaterialsAsList().stream().filter(material -> material instanceof SecondaryMaterial).map(SecondaryMaterial.class::cast).toList()) {
            for (Item toolPart : itemCollection.getToolParts()) {
                ForgeroToolPart part = ((ToolPartItem) toolPart).getPart();
                registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part), ModelLayer.SECONDARY, part.getPattern().getVariant()));
                if (part.getToolPartType() == ForgeroToolPartTypes.BINDING) {
                    registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.PICKAXE), ModelLayer.SECONDARY, part.getPattern().getVariant()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.SHOVEL), ModelLayer.SECONDARY, part.getPattern().getVariant()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.AXE), ModelLayer.SECONDARY, part.getPattern().getVariant()));
                }
                if (part.getToolPartType() == ForgeroToolPartTypes.HANDLE) {
                    registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.MEDIUMHANDLE, ModelLayer.SECONDARY, part.getPattern().getVariant()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.FULLHANDLE, ModelLayer.SECONDARY, part.getPattern().getVariant()));
                    registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.SHORTHANDLE, ModelLayer.SECONDARY, part.getPattern().getVariant()));
                }
            }
            for (Gem gem : ForgeroRegistry.getInstance().gemCollection().getGems()) {
                for (Item toolPart : itemCollection.getToolParts()) {
                    ForgeroToolPart toolPartItem = ((ToolPartItem) toolPart).getPart();

                    if (toolPartItem.getToolPartType() == ForgeroToolPartTypes.HEAD) {
                        registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.PICKAXEHEAD, ModelLayer.GEM, toolPartItem.getPattern().getVariant()));
                        registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.SHOVELHEAD, ModelLayer.GEM, toolPartItem.getPattern().getVariant()));
                        registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.AXEHEAD, ModelLayer.GEM, toolPartItem.getPattern().getVariant()));
                    }
                    if (toolPartItem.getToolPartType() == ForgeroToolPartTypes.BINDING) {
                        registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.BINDING, ModelLayer.GEM, toolPartItem.getPattern().getVariant()));
                        registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.PICKAXEBINDING, ModelLayer.GEM, toolPartItem.getPattern().getVariant()));
                        registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.SHOVELBINDING, ModelLayer.GEM, toolPartItem.getPattern().getVariant()));
                        registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.AXEHEADBINDING, ModelLayer.GEM, toolPartItem.getPattern().getVariant()));
                    }
                    if (toolPartItem.getToolPartType() == ForgeroToolPartTypes.HANDLE) {
                        registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.HANDLE, ModelLayer.GEM, toolPartItem.getPattern().getVariant()));
                        registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.SHORTHANDLE, ModelLayer.GEM, toolPartItem.getPattern().getVariant()));
                        registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.MEDIUMHANDLE, ModelLayer.GEM, toolPartItem.getPattern().getVariant()));
                        registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.FULLHANDLE, ModelLayer.GEM, toolPartItem.getPattern().getVariant()));
                    }
                }
            }

            ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
                List<ModelIdentifier> textures = registry.getTextures().stream().map(texture -> new ModelIdentifier(ForgeroInitializer.MOD_NAMESPACE, texture.getIdentifier() + "texture_dummy", "inventory")).toList();
                textures.forEach(out);
                out.accept(new ModelIdentifier(ForgeroInitializer.MOD_NAMESPACE, "transparent_base" + "texture_dummy", "inventory"));
            });
        }
    }
}
