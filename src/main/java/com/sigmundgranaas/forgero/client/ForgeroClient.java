package com.sigmundgranaas.forgero.client;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.forgerotool.model.ForgeroModelVariantProvider;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
import com.sigmundgranaas.forgero.client.forgerotool.model.UnbakedModelCollection;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.ItemCollection;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.util.ModelIdentifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class ForgeroClient implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);

    @Override
    public void onInitializeClient() {
        initializeItemModels();
    }

    private void initializeItemModels() {
        //ToolModelManager toolParts = new ToolModel3DManager(ForgeroItemCollection.INSTANCE.getToolPartHandles(), ItemInitializer.toolPartsHeads, ItemInitializer.toolPartsBindings);
        //ToolModelManager toolModels = new ToolModel2DManager(ItemCollection.INSTANCE.getTools(), ItemCollection.INSTANCE.getToolPartBindings());
        //ModelLoadingRegistry.INSTANCE.registerVariantProvider(rm -> new ForgeroToolModelProvider(toolModels));
        ItemCollection itemCollection = ItemCollection.INSTANCE;

        ModelLoadingRegistry.INSTANCE.registerVariantProvider(variant -> new ForgeroModelVariantProvider(UnbakedModelCollection.INSTANCE));
        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            itemCollection.getToolParts()
                    .stream()
                    .map(ToolPartItem.class::cast)
                    .forEach(toolPart -> {
                        out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, toolPart.getPart().getToolPartIdentifier(), "inventory"));
                        MaterialCollection.INSTANCE.getSecondaryMaterialsAsList()
                                .forEach(secondaryMaterial -> out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, secondaryMaterial.getName() + "_" + ToolPartModelType.getModelType(toolPart.getPart()).toFileName() + "_secondary", "inventory")));
                        itemCollection.getToolItems()
                                .stream()
                                .map(ForgeroToolItem.class::cast)
                                .forEach(forgeroToolItem -> {
                                    out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, toolPart.getPrimaryMaterial().getName() + "_" + ToolPartModelType.getModelType(toolPart.getPart(), forgeroToolItem.getToolType())));
                                    MaterialCollection.INSTANCE.getSecondaryMaterialsAsList()
                                            .forEach(secondaryMaterial -> out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, secondaryMaterial.getName() + "_" + ToolPartModelType.getModelType(toolPart.getPart(), forgeroToolItem.getToolType()).toFileName() + "_secondary", "inventory")));
                                });
                    });
            out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, "transparent_base", "inventory"));
        });
    }
}
