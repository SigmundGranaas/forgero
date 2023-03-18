package com.sigmundgranaas.forgero.minecraft.common.client.model;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.model.CompositeModelTemplate;
import com.sigmundgranaas.forgero.core.model.ModelRegistry;
import com.sigmundgranaas.forgero.core.model.ModelTemplate;
import com.sigmundgranaas.forgero.core.model.PaletteTemplateModel;
import com.sigmundgranaas.forgero.core.model.TextureBasedModel;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.client.ForgeroCustomModelProvider;
import com.sigmundgranaas.forgero.minecraft.common.client.forgerotool.model.implementation.EmptyBakedModel;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.minecraft.common.item.StateItem;

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
	private final LoadingCache<ItemStack, BakedModel> cache;
	private final LoadingCache<String, BakedModel> defaultCache;
	private final ModelRegistry registry;
	private Baker loader;
	private Function<SpriteIdentifier, Sprite> textureGetter;

	public CompositeModelVariant(ModelRegistry modelRegistry) {
		this.registry = modelRegistry;
		this.cache = CacheBuilder.newBuilder().maximumSize(600).build(new CacheLoader<>() {
			@Override
			public @NotNull
			BakedModel load(@NotNull ItemStack stack) {
				return converter(stack).flatMap((model) -> convertModel(model)).orElse(new EmptyBakedModel());
			}
		});
		this.defaultCache = CacheBuilder.newBuilder().maximumSize(600).build(new CacheLoader<>() {
			@Override
			public @NotNull
			BakedModel load(@NotNull String value) {
				return ForgeroStateRegistry.STATES
						.find(value)
						.map(Supplier::get)
						.flatMap(modelRegistry::find)
						.flatMap(modelTemplate -> convertModel(modelTemplate))
						.orElse(new EmptyBakedModel());
			}
		});
	}


	public BakedModel getModel(ItemStack stack) {
		if (stack.hasNbt() && stack.getOrCreateNbt().contains(FORGERO_IDENTIFIER)) {
			return cache.getUnchecked(stack);
		} else if (stack.getItem() instanceof StateItem stateItem) {
			try {
				return defaultCache.get(stateItem.identifier(), () -> getDefaultModel(stateItem));
			} catch (Exception e) {
				return new EmptyBakedModel();
			}
		}
		return new EmptyBakedModel();
	}

	private BakedModel getDefaultModel(State state) {
		return ForgeroStateRegistry
				.stateFinder()
				.find(state.identifier())
				.flatMap(registry::find)
				.flatMap(this::convertModel)
				.orElse(new EmptyBakedModel());
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
			defaultCache.invalidateAll();
		}
		return this;
	}
}
