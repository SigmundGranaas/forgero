package com.sigmundgranaas.forgero.item.forgerotool.model.dynamicmodel;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.model.ToolModelManager;
import com.sigmundgranaas.forgero.item.forgerotool.tool.item.ForgeroTool;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ToolModel2DManager implements ToolModelManager {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);
    private final List<Item> tools;
    private final HashMap<String, FabricBakedModel> models = new HashMap<>();

    public ToolModel2DManager(List<Item> tools) {
        this.tools = tools;
    }

    @Override
    public Optional<FabricBakedModel> getModel(@NotNull ItemStack tool) {
        Item item = tool.getItem();
        assert item instanceof ForgeroTool;

        FabricBakedModel toolModel = models.get(((ForgeroTool) item).getIdentifier().getPath());

        if (toolModel != null) {
            return Optional.of(toolModel);
        }
        return Optional.empty();
    }

    private void bakeAllModels(ModelLoader loader) {
        for (Item tool : tools) {
            assert tool instanceof ForgeroTool;
            DynamicModel[] models = Dynamic2DModelFactory.createModels((ForgeroTool) tool);
            this.models.put(models[0].getModelIdentifier().getPath(), (FabricBakedModel) models[0].bake(loader, null, null, null));
        }
    }

    @Override
    public void bakeModels(ModelLoader loader) {
        if (models.isEmpty()) {
            bakeAllModels(loader);
        }
    }
}

