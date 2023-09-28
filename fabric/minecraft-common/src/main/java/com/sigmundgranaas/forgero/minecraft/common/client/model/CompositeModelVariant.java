package com.sigmundgranaas.forgero.minecraft.common.client.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
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

import net.minecraft.client.render.model.Baker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class CompositeModelVariant extends ForgeroCustomModelProvider {
	public static final BakedModel EMPTY = new EmptyBakedModel();
	private final LoadingCache<StackContextKey, BakedModel> cache;
	private final ModelRegistry registry;
	private final StateService stateService;
	private Baker loader;
	private Function<SpriteIdentifier, Sprite> textureGetter;

	public CompositeModelVariant(ModelRegistry modelRegistry, StateService stateService) {
		this.registry = modelRegistry;
		this.stateService = stateService;
		this.cache = CacheBuilder.newBuilder()
				.expireAfterAccess(Duration.ofMinutes(1))
				.build(new CacheLoader<>() {
					@Override
					public @NotNull
					BakedModel load(@NotNull StackContextKey pair) {
						return converter(pair.stack(), pair.context()).flatMap((model) -> convertModel(model)).orElse(new EmptyBakedModel());
					}
				});
	}

	public BakedModel getModel(ItemStack stack, MatchContext context) {
		if (ForgeroConfigurationLoader.configuration.buildModelsAsync) {
			return getModelAsync(stack, context);
		} else {
			StackContextKey key = new StackContextKey(stack, context);
			try {
				return cache.get(key);
			} catch (ExecutionException e) {
				return EMPTY;
			}
		}
	}

	public BakedModel getModelAsync(ItemStack stack, MatchContext context) {
		StackContextKey key = new StackContextKey(stack, context);
		BakedModel model = cache.getIfPresent(key);
		if (model != null) {
			return model;
		}

		DynamicBakedModel dynamicModel = new DynamicBakedModel(); // Will initially be an EmptyBakedModel
		cache.put(key, dynamicModel);  // Cache the dynamic model

		// Compute the model data asynchronously
		CompletableFuture.supplyAsync(() -> {
			try {
				return converter(key.stack(), key.context())
						.flatMap(this::convertModel)
						.orElse(EMPTY);
			} catch (Exception e) {
				return EMPTY;
			}
		}).thenAccept(dynamicModel::updateModel);

		return dynamicModel;
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
