package com.sigmundgranaas.forgero.client;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.forgerotool.model.*;
import com.sigmundgranaas.forgero.item.ItemInitializer;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
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
        ToolModelManager toolParts = new ToolModel3DManager(ItemInitializer.toolPartsHandles, ItemInitializer.toolPartsHeads, ItemInitializer.toolPartsBindings);
        ToolModelManager toolModels = new ToolModel2DManager(ItemInitializer.tools, ItemInitializer.toolPartsBindings);
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(rm -> new ForgeroToolModelProvider(toolModels));
        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            for (ForgeroToolPartItem part : ItemInitializer.toolPartsBindings) {
                out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getMaterial().toString().toLowerCase() + "_" + ToolPartModelType.PICKAXEBINDING.toFileName(), "inventory"));
                out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getMaterial().toString().toLowerCase() + "_" + ToolPartModelType.SHOVELBINDING.toFileName(), "inventory"));
            }
            for (ForgeroToolPartItem part : ItemInitializer.toolPartsHandles) {
                out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getMaterial().toString().toLowerCase() + "_" + ToolPartModelType.FULLHANDLE.toFileName(), "inventory"));
                out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getMaterial().toString().toLowerCase() + "_" + ToolPartModelType.MEDIUMHANDLE.toFileName(), "inventory"));
                out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getMaterial().toString().toLowerCase() + "_" + ToolPartModelType.SHORTHANDLE.toFileName(), "inventory"));
            }

        });
    }
}
