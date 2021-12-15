package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.sigmundgranaas.forgero.Forgero;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;

import java.util.Optional;
import java.util.function.Function;

public interface ToolModelManager {
    Optional<FabricBakedModel> getModel(ItemStack stack);

    void bakeModels(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter);

    UnbakedModel getUnbakedModel(ModelIdentifier id);

    default boolean isQualifiedModelManager(ModelIdentifier id) {
        return id.getNamespace().equals(Forgero.MOD_NAMESPACE) && id.getPath().startsWith("pickaxe") || id.getPath().startsWith("shovel");
    }
}
