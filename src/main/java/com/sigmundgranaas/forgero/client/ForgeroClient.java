package com.sigmundgranaas.forgero.client;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.ItemInitializer;
import com.sigmundgranaas.forgero.item.forgerotool.model.ForgeroToolModelProvider;
import com.sigmundgranaas.forgero.item.forgerotool.model.ToolModel2DManager;
import com.sigmundgranaas.forgero.item.forgerotool.model.ToolModel3DManager;
import com.sigmundgranaas.forgero.item.forgerotool.model.ToolModelManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
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
        ToolModelManager toolParts = new ToolModel3DManager(ItemInitializer.toolPartsHandles, ItemInitializer.toolPartsHeads, ItemInitializer.toolPartsBindings);
        ToolModelManager toolModels = new ToolModel2DManager(ItemInitializer.tools, ItemInitializer.toolPartsBindings);
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(rm -> new ForgeroToolModelProvider(toolModels));
    }
}
