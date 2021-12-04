package com.sigmundgranaas.forgero.item.forgerotool.model;

import com.sigmundgranaas.forgero.Forgero;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

public class ForgeroToolModel extends AbstractForgeroToolModel {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);
    private final ToolModelManager modelManager;

    public ForgeroToolModel(ToolModelManager modelManager) {
        this.modelManager = modelManager;
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        modelManager.getModel(stack).ifPresent(model -> model.emitItemQuads(null, null, context));
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        modelManager.bakeModels(loader);
        return this;
    }
}
