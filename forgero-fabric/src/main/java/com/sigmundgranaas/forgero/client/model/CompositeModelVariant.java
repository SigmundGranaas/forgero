package com.sigmundgranaas.forgero.client.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.client.ForgeroCustomModelProvider;
import com.sigmundgranaas.forgero.client.forgerotool.model.implementation.EmptyBakedModel;
import com.sigmundgranaas.forgero.conversion.StateConverter;
import com.sigmundgranaas.forgero.model.*;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
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
    private final LoadingCache<ItemStack, FabricBakedModel> cache;
    private final ModelRegistry registry;
    private Baker loader;
    private Function<SpriteIdentifier, Sprite> textureGetter;

    public CompositeModelVariant(ModelRegistry modelRegistry) {
        this.registry = modelRegistry;
        this.cache = CacheBuilder.newBuilder().maximumSize(600).build(new CacheLoader<>() {
            @Override
            public @NotNull FabricBakedModel load(@NotNull ItemStack stack) {
                return converter(stack).flatMap((model) -> convertModel(model)).orElse(new EmptyBakedModel());
            }
        });
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        cache.getUnchecked(stack).emitItemQuads(null, null, context);
    }

    public BakedModel getModel(ItemStack stack) {
        return ((BakedModel) cache.getUnchecked(stack));
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

    @Override
    public void setParents(Function<Identifier, UnbakedModel> modelLoader) {

    }

    @Nullable
    @Override
    public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        if (this.loader == null || this.loader != baker) {
            this.loader = baker;
            this.textureGetter = textureGetter;
            cache.invalidateAll();
        }
        return this;
    }
}
