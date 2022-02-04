package com.sigmundgranaas.forgero.client;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.forgerotool.model.ForgeroModelVariantProvider;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.client.forgerotool.model.UnbakedModelCollection;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
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
import java.util.Set;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class ForgeroClient implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);

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
        MaterialCollection materialCollection = MaterialCollection.INSTANCE;
        Set<ModelIdentifier> modelSet = new HashSet<>();


        for (PrimaryMaterial material : materialCollection.getMaterialsAsList().stream().filter(material -> material instanceof PrimaryMaterial).map(PrimaryMaterial.class::cast).collect(Collectors.toList())) {
            for (Item toolPart : itemCollection.getToolParts()) {
                modelSet.add(createToolPartModelIdentifier(((ToolPartItem) toolPart).getPart()));
                modelSet.add(createToolPartModelIdentifier(material, ((ToolPartItem) toolPart).getPart(), ForgeroToolTypes.PICKAXE));
                modelSet.add(createToolPartModelIdentifier(material, ((ToolPartItem) toolPart).getPart(), ForgeroToolTypes.SHOVEL));
            }
        }

        for (SecondaryMaterial material : materialCollection.getMaterialsAsList().stream().filter(material -> material instanceof SecondaryMaterial).map(SecondaryMaterial.class::cast).collect(Collectors.toList())) {
            for (Item toolPart : itemCollection.getToolParts()) {
                modelSet.add(createToolPartModelIdentifier(material, ((ToolPartItem) toolPart).getPart()));
                modelSet.add(createToolPartModelIdentifier(material, ((ToolPartItem) toolPart).getPart(), ForgeroToolTypes.PICKAXE));
                modelSet.add(createToolPartModelIdentifier(material, ((ToolPartItem) toolPart).getPart(), ForgeroToolTypes.SHOVEL));
            }
        }

        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            modelSet.forEach(out);
            out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, "transparent_base", "inventory"));
        });
    }

    private ModelIdentifier createToolPartModelIdentifier(PrimaryMaterial material, ForgeroToolPart toolPart, ForgeroToolTypes toolTypes) {
        return new ModelIdentifier(Forgero.MOD_NAMESPACE, material.getName() + "_" + ToolPartModelType.getModelType(toolPart, toolTypes).toFileName() + "texture_dummy", "inventory");

    }

    private ModelIdentifier createToolPartModelIdentifier(ForgeroToolPart toolPart) {
        return new ModelIdentifier(Forgero.MOD_NAMESPACE, toolPart.getToolPartIdentifier() + "texture_dummy", "inventory");
    }

    private ModelIdentifier createToolPartModelIdentifier(SecondaryMaterial material, ForgeroToolPart toolPart, ForgeroToolTypes toolTypes) {
        return new ModelIdentifier(Forgero.MOD_NAMESPACE, material.getName() + "_" + ToolPartModelType.getModelType(toolPart, toolTypes).toFileName() + "_secondary" + "texture_dummy", "inventory");
    }

    private ModelIdentifier createToolPartModelIdentifier(SecondaryMaterial material, ForgeroToolPart toolPart) {
        return new ModelIdentifier(Forgero.MOD_NAMESPACE, material.getName() + "_" + ToolPartModelType.getModelType(toolPart).toFileName() + "_secondary" + "texture_dummy", "inventory");
    }
}
