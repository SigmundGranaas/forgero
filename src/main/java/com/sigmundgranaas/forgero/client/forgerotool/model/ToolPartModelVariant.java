package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.client.forgerotool.model.implementation.EmptyBakedModelCollection;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

public class ToolPartModelVariant extends ForgeroCustomModelProvider {
    private final UnbakedModelCollection unbakedModelCollection;
    private BakedModelCollection bakedModelCollection;
    private final LoadingCache<ItemStack, FabricBakedModel> cache;

    public ToolPartModelVariant(UnbakedModelCollection collection) {
        this.unbakedModelCollection = collection;
        bakedModelCollection = new EmptyBakedModelCollection();
       this.cache = CacheBuilder.newBuilder().maximumSize(600).build(new CacheLoader<>() {
            @Override
            public @NotNull FabricBakedModel load(@NotNull ItemStack stack) {
                return bakedModelCollection.getToolPartModel(stack);
            }
        });
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        cache.getUnchecked(stack).emitItemQuads(null, null, context);
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        this.bakedModelCollection = unbakedModelCollection.bakeModels(loader, textureGetter);
        return this;
    }
}
