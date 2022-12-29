package com.sigmundgranaas.forgero.minecraft.common.client.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.minecraft.common.client.forgerotool.model.implementation.EmptyBakedModel;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.model.*;
import com.sigmundgranaas.forgero.minecraft.common.client.ForgeroCustomModelProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CompositeModelVariant extends ForgeroCustomModelProvider {
    private final LoadingCache<ItemStack, BakedModel> cache;
    private final ModelRegistry registry;
    private ModelLoader loader;
    private Function<SpriteIdentifier, Sprite> textureGetter;

    public CompositeModelVariant(ModelRegistry modelRegistry) {
        this.registry = modelRegistry;
        this.cache = CacheBuilder.newBuilder().maximumSize(600).build(new CacheLoader<>() {
            @Override
            public @NotNull BakedModel load(@NotNull ItemStack stack) {
                return converter(stack).flatMap((model) -> convertModel(model)).orElse(new EmptyBakedModel());
            }
        });
    }


    public BakedModel getModel(ItemStack stack) {
        return (cache.getUnchecked(stack));
    }

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        if (this.loader == null || this.loader != loader) {
            this.loader = loader;
            this.textureGetter = textureGetter;
            cache.invalidateAll();
        }
        return this;
    }

    private Optional<BakedModel> convertModel(ModelTemplate template) {
        var unbakedModel = template.convert(this::modelConverter);
        if (unbakedModel.isPresent()) {
            return unbakedModel.map(UnbakedDynamicModel::bake);
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

    private Optional<UnbakedDynamicModel> modelConverter(ModelTemplate input) {
        var textureList = new ArrayList<PaletteTemplateModel>();
        if (input instanceof CompositeModelTemplate model) {
            model.getModels().forEach(template -> textureCollector(template, textureList));
            var unbakedModel = new Unbaked2DTexturedModel(loader, textureGetter, textureList, "dummy");
            return Optional.of(unbakedModel);
        } else if (input instanceof TextureBasedModel model) {
            textureCollector(model, textureList);
            var unbakedModel = new Unbaked2DTexturedModel(loader, textureGetter, textureList, model.getTexture());
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

}
