package com.sigmundgranaas.forgero.minecraft.common.client.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.model.CompositeModelTemplate;
import com.sigmundgranaas.forgero.core.model.ModelRegistry;
import com.sigmundgranaas.forgero.core.model.ModelResult;
import com.sigmundgranaas.forgero.core.model.ModelTemplate;
import com.sigmundgranaas.forgero.core.model.PaletteTemplateModel;
import com.sigmundgranaas.forgero.core.model.TextureBasedModel;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.core.util.match.MutableMatchContext;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CompositeModelVariant extends ForgeroCustomModelProvider {
	public static final BakedModel EMPTY = new EmptyBakedModel();
	private static final Set<StackContextKey> currentlyBuilding = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private final LoadingCache<StackContextKey, BakedModelResult> cache;
	private final LoadingCache<String, BakedModelResult> keyModelsCache;
	private final LoadingCache<Item, BakedModelResult> vanillaModelCache;
	private final ModelRegistry registry;
	private final StateService stateService;
	private ModelLoader loader;
	private Function<SpriteIdentifier, Sprite> textureGetter;

	public CompositeModelVariant(ModelRegistry modelRegistry, StateService stateService) {
		this.registry = modelRegistry;
		this.stateService = stateService;
		this.cache = CacheBuilder.newBuilder()
				.expireAfterAccess(Duration.ofMinutes(1))
				.build(new CacheLoader<>() {
					@Override
					public @NotNull
					BakedModelResult load(@NotNull StackContextKey pair) {
						return converter(pair.stack(), MatchContext.mutable(pair.context())).flatMap((model) -> convertModel(model)).orElse(new BakedModelResult(new ModelResult(), EMPTY));
					}
				});


		this.keyModelsCache = CacheBuilder.newBuilder()
				.softValues()
				.maximumSize(100)
				.build(new CacheLoader<>() {
					@Override
					public BakedModelResult load(String key) {
						return new BakedModelResult(new ModelResult(), EMPTY);
					}
				});
		this.vanillaModelCache = CacheBuilder.newBuilder()
				.softValues()
				.maximumSize(100)
				.build(new CacheLoader<>() {
					@Override
					public BakedModelResult load(Item key) {
						return new BakedModelResult(new ModelResult(), EMPTY);
					}
				});
	}

	public BakedModel getModel(ItemStack stack, MatchContext context) {
		if (ForgeroConfigurationLoader.configuration.buildModelsAsync) {
			return getModelAsync(stack, context);
		} else {
			StackContextKey key = new StackContextKey(stack, context);
			try {
				BakedModelResult result = cache.get(key);
				if (result.result().isValid(Matchable.DEFAULT_TRUE, context)) {
					return result.model();
				} else {
					cache.refresh(key);
					return cache.get(key).model();
				}
			} catch (ExecutionException e) {
				return EMPTY;
			}
		}
	}

	public BakedModel getModelAsync(ItemStack stack, MatchContext context) {
		StackContextKey key = new StackContextKey(stack, context);
		BakedModelResult model = cache.getIfPresent(key);


		// If the model exists and is valid, return it
		if (model != null && model.result().isValid(Matchable.DEFAULT_TRUE, context)) {
			return model.model();
		}

		// If the model is currently being built, or if it's invalid (but not currently being built),
		// return the existing (possibly invalid) model but trigger a rebuild if it's not already rebuilding
		if (model != null) {
			if (!currentlyBuilding.contains(key)) {
				currentlyBuilding.add(key);
				triggerAsyncRebuild(key, model);
			}
			return model.model();
		}


		// If there's no model at all (first time fetching for this key)
		DynamicBakedModel dynamicModel = new DynamicBakedModel();
		ModelResult result = new ModelResult();
		BakedModelResult baked = new BakedModelResult(result, dynamicModel);

		// Cache the dynamic model temporarily
		cache.put(key, baked);

		if (!currentlyBuilding.contains(key)) {
			currentlyBuilding.add(key);
			triggerAsyncRebuild(key, baked);
		}

		if (stack.hasNbt() && stack.getNbt().contains("ModelCacheKey")) {
			if (keyModelsCache.asMap().containsKey(stack.getNbt().getString("ModelCacheKey"))) {
				return keyModelsCache.getUnchecked(stack.getNbt().getString("ModelCacheKey")).model();
			}
		} else if (stack.hasNbt()) {
			stack.getNbt().putString("ModelCacheKey", UUID.randomUUID().toString());
		} else if (!stack.hasNbt() && vanillaModelCache.asMap().containsKey(stack.getItem())) {
			return vanillaModelCache.getUnchecked(stack.getItem()).model();
		}

		return dynamicModel;
	}

	private void triggerAsyncRebuild(StackContextKey key, BakedModelResult currentModel) {
		MutableMatchContext ctx = MatchContext.mutable(key.context().put(ModelResult.MODEL_RESULT, new ModelResult()));

		// Compute the model data asynchronously
		CompletableFuture.supplyAsync(() -> {
			try {
				return converter(key.stack(), ctx)
						.flatMap(this::convertModel)
						.orElse(currentModel);
			} catch (Exception e) {
				return currentModel;
			}
		}).thenAccept((m) -> {

			if (key.stack().hasNbt() && key.stack().getNbt().contains("ModelCacheKey")) {
				String cacheKey = key.stack().getNbt().getString("ModelCacheKey");
				keyModelsCache.put(cacheKey, m);
			} else {
				vanillaModelCache.put(key.stack().getItem(), m);
			}
			cache.put(key, m);
			currentlyBuilding.remove(key);  // Model has been built, remove from tracking set
		});
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

	private Optional<BakedModelResult> convertModel(ModelResult template) {
		var unbakedModel = template.getTemplate().convert(this::modelConverter);
		if (unbakedModel.isPresent()) {
			return unbakedModel.map(UnbakedDynamicModel::bake)
					.map(baked -> new BakedModelResult(template, baked));
		}
		return Optional.empty();
	}

	private Optional<ModelResult> converter(ItemStack stack, MatchContext context) {
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


