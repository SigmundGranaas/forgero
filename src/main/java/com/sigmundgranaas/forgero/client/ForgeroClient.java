package com.sigmundgranaas.forgero.client;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.client.forgerotool.model.ForgeroModelVariantProvider;
import com.sigmundgranaas.forgero.client.forgerotool.model.ModelLayer;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.client.forgerotool.model.UnbakedModelCollection;
import com.sigmundgranaas.forgero.client.texture.FabricTextureIdentifierFactory;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.gem.BindingGem;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.HandleGem;
import com.sigmundgranaas.forgero.core.gem.HeadGem;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.ToolPartModelTextureIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.texture.ForgeroToolPartTextureRegistry;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
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
import java.util.stream.Collectors;

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


        for (PrimaryMaterial material : materialCollection.getMaterialsAsList().stream().filter(material -> material instanceof PrimaryMaterial).map(PrimaryMaterial.class::cast).collect(Collectors.toList())) {
            for (Item toolPart : itemCollection.getToolParts()) {
                ForgeroToolPart part = ((ToolPartItem) toolPart).getPart();
                registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part), ModelLayer.PRIMARY, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
                registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.PICKAXE), ModelLayer.PRIMARY, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
                registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.SHOVEL), ModelLayer.PRIMARY, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
                registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.AXE), ModelLayer.PRIMARY, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
            }
        }

        for (SecondaryMaterial material : materialCollection.getMaterialsAsList().stream().filter(material -> material instanceof SecondaryMaterial).map(SecondaryMaterial.class::cast).collect(Collectors.toList())) {
            for (Item toolPart : itemCollection.getToolParts()) {
                ForgeroToolPart part = ((ToolPartItem) toolPart).getPart();
                registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part), ModelLayer.SECONDARY, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
                registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.PICKAXE), ModelLayer.SECONDARY, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
                registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.SHOVEL), ModelLayer.SECONDARY, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
                registry.registerTexture(new ToolPartModelTextureIdentifier(material.getName(), ToolPartModelType.getModelType(part, ForgeroToolTypes.AXE), ModelLayer.SECONDARY, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
            }
        }
        for (Gem gem : ForgeroRegistry.getInstance().gemCollection().getGems()) {
            if (gem instanceof HeadGem) {
                registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.PICKAXEHEAD, ModelLayer.GEM, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
                registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.SHOVELHEAD, ModelLayer.GEM, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
                registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.AXEHEAD, ModelLayer.GEM, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
            }
            if (gem instanceof BindingGem) {
                registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.BINDING, ModelLayer.GEM, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
                registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.PICKAXEBINDING, ModelLayer.GEM, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
                registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.SHOVELBINDING, ModelLayer.GEM, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
                registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.AXEHEADBINDING, ModelLayer.GEM, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
            }
            if (gem instanceof HandleGem) {
                registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.HANDLE, ModelLayer.GEM, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
                registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.SHORTHANDLE, ModelLayer.GEM, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
                registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.MEDIUMHANDLE, ModelLayer.GEM, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
                registry.registerTexture(new ToolPartModelTextureIdentifier(gem.getName(), ToolPartModelType.FULLHANDLE, ModelLayer.GEM, ToolPartModelTextureIdentifier.DEFAULT_SKIN_IDENTIFIER));
            }
        }

        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            //modelSet.forEach(out);
            List<ModelIdentifier> textures = registry.getTextures().stream().map(texture -> new ModelIdentifier(ForgeroInitializer.MOD_NAMESPACE, texture.getIdentifier() + "texture_dummy", "inventory")).collect(Collectors.toList());
            textures.forEach(out);
            out.accept(new ModelIdentifier(ForgeroInitializer.MOD_NAMESPACE, "transparent_base" + "texture_dummy", "inventory"));
            //out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, "pickaxehead_gem" + "texture_dummy", "inventory"));
            //out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, "handle_gem" + "texture_dummy", "inventory"));
            //out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, "fullhandle_gem" + "texture_dummy", "inventory"));
        });
    }

    private void createGemModels(Gem gem) {

    }

    private ModelIdentifier createToolPartModelIdentifier(PrimaryMaterial material, ForgeroToolPart toolPart, ForgeroToolTypes toolTypes) {
        return new ModelIdentifier(ForgeroInitializer.MOD_NAMESPACE, material.getName() + "_" + ToolPartModelType.getModelType(toolPart, toolTypes).toFileName() + "_primary_default" + "texture_dummy", "inventory");

    }

    private ModelIdentifier createToolPartModelIdentifier(ForgeroToolPart toolPart) {
        return new ModelIdentifier(ForgeroInitializer.MOD_NAMESPACE, toolPart.getToolPartIdentifier() + "_primary_default" + "texture_dummy", "inventory");
    }

    private ModelIdentifier createToolPartModelIdentifier(SecondaryMaterial material, ForgeroToolPart toolPart, ForgeroToolTypes toolTypes) {
        return new ModelIdentifier(ForgeroInitializer.MOD_NAMESPACE, material.getName() + "_" + ToolPartModelType.getModelType(toolPart, toolTypes).toFileName() + "_secondary_default" + "texture_dummy", "inventory");
    }

    private ModelIdentifier createToolPartModelIdentifier(SecondaryMaterial material, ForgeroToolPart toolPart) {
        return new ModelIdentifier(ForgeroInitializer.MOD_NAMESPACE, material.getName() + "_" + ToolPartModelType.getModelType(toolPart).toFileName() + "_secondary_default" + "texture_dummy", "inventory");
    }
}
