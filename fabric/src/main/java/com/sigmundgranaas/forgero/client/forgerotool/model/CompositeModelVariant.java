package com.sigmundgranaas.forgero.client.forgerotool.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.client.forgerotool.model.implementation.EmptyBakedModelCollection;
import com.sigmundgranaas.forgero.client.forgerotool.model.toolpart.UnbakedTextureModel;
import com.sigmundgranaas.forgero.model.CompositeModelTemplate;
import com.sigmundgranaas.forgero.model.ModelRegistry;
import com.sigmundgranaas.forgero.model.ModelTemplate;
import com.sigmundgranaas.forgero.model.PaletteTemplateModel;
import com.sigmundgranaas.forgero.state.Composite;
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
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        converter(stack.getItem()).flatMap(this::convertModel).ifPresent(model -> model.emitItemQuads(null, null, context));

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
            return unbakedModel.map(UnbakedTextureModel::bake);
        }
        return Optional.empty();
    }

    private Optional<ModelTemplate> converter(Item item) {
        String id = Registry.ITEM.getId(item).toString();
        var compositeOpt = com.sigmundgranaas.forgero.Registry.STATES.get(id).map(Composite.class::cast);
        if (compositeOpt.isPresent()) {
            var composite = compositeOpt.get();
            return registry.find(composite);
        }
        return Optional.empty();
    }

    private Optional<UnbakedTextureModel> modelConverter(ModelTemplate input) {
        if (input instanceof CompositeModelTemplate model) {
            if (model.getModels().size() > 0) {
                if (model.getModels().get(0) instanceof PaletteTemplateModel paletteModel) {
                    return Optional.of(templateToModel(paletteModel));
                }
            }
        }
        return Optional.empty();
    }

    private UnbakedTextureModel templateToModel(PaletteTemplateModel model) {
        String texture = String.format("%s-%s", model.palette(), model.template().replace(".png", ""));
        return new UnbakedTextureModel(loader, textureGetter, texture, texture, 1);
    }
}
