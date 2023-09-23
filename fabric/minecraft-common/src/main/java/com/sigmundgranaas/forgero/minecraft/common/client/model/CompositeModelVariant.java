package com.sigmundgranaas.forgero.minecraft.common.client.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.core.model.CompositeModelTemplate;
import com.sigmundgranaas.forgero.core.model.ModelRegistry;
import com.sigmundgranaas.forgero.core.model.ModelTemplate;
import com.sigmundgranaas.forgero.core.model.PaletteTemplateModel;
import com.sigmundgranaas.forgero.core.model.TextureBasedModel;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.ForgeroCustomModelProvider;
import com.sigmundgranaas.forgero.minecraft.common.client.forgerotool.model.implementation.EmptyBakedModel;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CompositeModelVariant extends ForgeroCustomModelProvider {
	private static final BakedModel EMPTY = new EmptyBakedModel();
	private final LoadingCache<StackContextKey, BakedModel> cache;
	private final ModelRegistry registry;
	private final StateService stateService;
	private final ConcurrentHashMap<StackContextKey, CompletableFuture<BakedModel>> futureCache = new ConcurrentHashMap<>();
	private ModelLoader loader;
	private Function<SpriteIdentifier, Sprite> textureGetter;

	public CompositeModelVariant(ModelRegistry modelRegistry, StateService stateService) {
		this.registry = modelRegistry;
		this.stateService = stateService;
		this.cache = CacheBuilder.newBuilder()
				.maximumSize(4000)
				.expireAfterAccess(Duration.ofSeconds(10))
				.build(new CacheLoader<>() {
					@Override
					public @NotNull
					BakedModel load(@NotNull StackContextKey pair) {
						return converter(pair.stack(), pair.context()).flatMap((model) -> convertModel(model)).orElse(new EmptyBakedModel());
					}
				});
	}


	public BakedModel getModel(ItemStack stack, MatchContext context) {
		StackContextKey key = new StackContextKey(stack, context);
		BakedModel model = cache.getIfPresent(key);
		if (model != null) {
			return model;
		}

		// Return existing future or compute a new one
		CompletableFuture<BakedModel> future = futureCache.computeIfAbsent(key, k -> CompletableFuture.supplyAsync(() -> {
			try {
				return cache.getUnchecked(k);
			} catch (Exception e) {
				return EMPTY;
			}
		}));

		// Attach callback to future to update the main cache when model is built
		future.thenAccept(bakedModel -> {
			cache.put(key, bakedModel);
			futureCache.remove(key); // cleanup future from map once done
		});

		return EMPTY;

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

	private Optional<ModelTemplate> converter(ItemStack stack, MatchContext context) {
		var compositeOpt = stateService.convert(stack);
		if (compositeOpt.isPresent()) {
			var composite = compositeOpt.get();
			return registry.find(composite, MatchContext.mutable(context));
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


