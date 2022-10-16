package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.client.forgerotool.model.implementation.EmptyBakedModelCollection;
import com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.SingleTexturedModel;
import com.sigmundgranaas.forgero.client.model.Unbaked2DTexturedModel;
import com.sigmundgranaas.forgero.client.model.UnbakedFabricModel;
import com.sigmundgranaas.forgero.conversion.StateConverter;
import com.sigmundgranaas.forgero.model.*;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompositeModelVariant extends ForgeroCustomModelProvider {
    private final UnbakedModelCollection unbakedModelCollection;
    private final LoadingCache<ItemStack, FabricBakedModel> cache;
    private final LoadingCache<Item, FabricBakedModel> itemCache;
    private final ModelRegistry registry;
    private BakedModelCollection bakedModelCollection;
    private ModelLoader loader;
    private Function<SpriteIdentifier, Sprite> textureGetter;

    public CompositeModelVariant(UnbakedModelCollection collection, ModelRegistry modelRegistry) {
        this.unbakedModelCollection = collection;
        this.registry = modelRegistry;
        bakedModelCollection = new EmptyBakedModelCollection();
        this.cache = CacheBuilder.newBuilder().maximumSize(600).build(new CacheLoader<>() {
            @Override
            public @NotNull FabricBakedModel load(@NotNull ItemStack stack) {
                return bakedModelCollection.getModel(stack);
            }
        });
        this.itemCache = CacheBuilder.newBuilder().maximumSize(600).build(new CacheLoader<>() {
            @Override
            public @NotNull FabricBakedModel load(@NotNull Item item) {
                return bakedModelCollection.getModel(item);
            }
        });
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        converter(stack).flatMap(this::convertModel).ifPresent(model -> model.emitItemQuads(null, null, context));
        /**
         * if (stack.hasNbt()) {
         *             cache.getUnchecked(stack).emitItemQuads(null, null, context);
         *         } else {
         *             itemCache.getUnchecked(stack.getItem()).emitItemQuads(null, null, context);
         *         }
         */


    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        this.bakedModelCollection = unbakedModelCollection.bakeModels(loader, textureGetter);
        if (this.loader == null || this.loader != loader) {
            this.loader = loader;
            this.textureGetter = textureGetter;
            cache.invalidateAll();
            itemCache.invalidateAll();
        }
        return this;
    }

    private Optional<FabricBakedModel> convertModel(ModelTemplate template) {
        var unbakedModel = template.convert(this::modelConverter);
        if (unbakedModel.isPresent()) {
            return unbakedModel.map(UnbakedFabricModel::bake);
        }
        return Optional.empty();
    }

    private Optional<ModelTemplate> converter(ItemStack stack) {
        var compositeOpt = StateConverter.of(stack);
        if (compositeOpt.isPresent()) {
            var composite = compositeOpt.get();
            return registry.find(composite);
        }
        return Optional.empty();
    }

    private Optional<UnbakedFabricModel> modelConverter(ModelTemplate input) {
        var textureList = new ArrayList<PaletteTemplateModel>();
        if (input instanceof CompositeModelTemplate model) {
            model.getModels().forEach(template -> textureCollector(template, textureList));
            var unbakedModel = new Unbaked2DTexturedModel(loader, textureGetter, textureList.stream().sorted().map(this::textureName).toList(), "test");
            return Optional.of(unbakedModel);
        } else if (input instanceof TextureBasedModel model) {
            textureCollector(model, textureList);
            var unbakedModel = new Unbaked2DTexturedModel(loader, textureGetter, textureList.stream().sorted().map(this::textureName).toList(), "test");
            return Optional.of(unbakedModel);
        }
        return Optional.empty();
    }

    private void textureCollector(ModelTemplate template, List<PaletteTemplateModel> accumulator) {
        if (template instanceof PaletteTemplateModel palette) {
            textureCollector(palette, accumulator);
        } else if (template instanceof CompositeModelTemplate composite) {
            textureCollector(composite, accumulator);
        }
    }

    private void textureCollector(PaletteTemplateModel template, List<PaletteTemplateModel> accumulator) {
        accumulator.add(template);
    }

    private void textureCollector(CompositeModelTemplate template, List<PaletteTemplateModel> accumulator) {
        template.getModels().forEach(model -> textureCollector(model, accumulator));
    }


    private UnbakedFabricModel templateToModel(PaletteTemplateModel model) {
        return new SingleTexturedModel(loader, textureGetter, textureName(model), textureName(model), 1);
    }

    private String textureName(PaletteTemplateModel model) {
        return String.format("%s-%s", model.palette(), model.template().replace(".png", ""));
    }
}
