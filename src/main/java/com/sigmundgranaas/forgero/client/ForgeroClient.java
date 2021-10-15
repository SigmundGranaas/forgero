package com.sigmundgranaas.forgero.client;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.ItemInitializer;
import com.sigmundgranaas.forgero.item.forgerotool.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.item.forgerotool.model.ForgeroModelResourceProvider;
import com.sigmundgranaas.forgero.item.forgerotool.model.ForgeroPartModels;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.minecraft.client.util.ModelIdentifier;

@Environment(EnvType.CLIENT)
public class ForgeroClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        initializeItemModels();
    }

    private void initializeItemModels() {
        ItemInitializer initializer = new ItemInitializer(ForgeroMaterial.getMaterialList());
        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            for (ForgeroToolPartItem part : initializer.getToolPartsBindings()) {
                out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase(), "inventory"));
            }
            for (ForgeroToolPartItem part : initializer.getToolPartsBindings()) {
                out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase() + "_shovel", "inventory"));
            }
            for (ForgeroToolPartItem part : initializer.getToolPartsHandles()) {
                out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase(), "inventory"));
            }
            for (ForgeroToolPartItem part : initializer.getToolPartsHeads()) {
                out.accept(new ModelIdentifier(Forgero.MOD_NAMESPACE, part.getToolPartTypeAndMaterialLowerCase(), "inventory"));
            }
        });
        ForgeroPartModels partModels = new ForgeroPartModels(initializer.getToolPartsHandles(), initializer.getToolPartsHeads(), initializer.getToolPartsBindings());
        ModelLoadingRegistry.INSTANCE.registerVariantProvider(rm -> new ForgeroModelResourceProvider(partModels));
    }
}
