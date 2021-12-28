package com.sigmundgranaas.forgero.client;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.client.forgerotool.model.ForgeroToolModelProvider;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolModel2DManager;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolModelManager;
import com.sigmundgranaas.forgero.client.forgerotool.model.ToolPartModelType;
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
        ToolModelManager toolModels = new ToolModel2DManager(ItemCollection.INSTANCE.getTools(), ItemCollection.INSTANCE.getToolPartBindings());
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(rm -> new ForgeroToolModelProvider(toolModels));
        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            for (ToolPartItem part : ItemCollection.INSTANCE.getToolPartBindings()) {
                out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getPrimaryMaterial().getName() + "_" + ToolPartModelType.PICKAXEBINDING.toFileName(), "inventory"));
                out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getPrimaryMaterial().getName() + "_" + ToolPartModelType.SHOVELBINDING.toFileName(), "inventory"));
            }
            for (ToolPartItem part : ItemCollection.INSTANCE.getToolPartHandles()) {
                out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getPrimaryMaterial().getName() + "_" + ToolPartModelType.FULLHANDLE.toFileName(), "inventory"));
                out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getPrimaryMaterial().getName() + "_" + ToolPartModelType.MEDIUMHANDLE.toFileName(), "inventory"));
                out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getPrimaryMaterial().getName() + "_" + ToolPartModelType.SHORTHANDLE.toFileName(), "inventory"));
            }

        });
    }
}
