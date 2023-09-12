package com.sigmundgranaas.forgero.minecraft.common.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

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
import net.minecraft.util.Pair;

public class CompositeModelVariant extends ForgeroCustomModelProvider {
	private final LoadingCache<Pair<ItemStack, MatchContext>, BakedModel> cache;
	private final ModelRegistry registry;
	private final StateService stateService;
	private Baker loader;
	private Function<SpriteIdentifier, Sprite> textureGetter;

	public CompositeModelVariant(ModelRegistry modelRegistry, StateService stateService) {
		this.registry = modelRegistry;
		this.stateService = stateService;
		this.cache = CacheBuilder.newBuilder().maximumSize(600).build(new CacheLoader<>() {
			@Override
			public @NotNull
			BakedModel load(@NotNull Pair<ItemStack, MatchContext> pair) {
				return converter(pair.getLeft(), pair.getRight()).flatMap((model) -> convertModel(model)).orElse(new EmptyBakedModel());
			}
		});
	}


	public BakedModel getModel(ItemStack stack, MatchContext context) {
		try {
			return cache.getUnchecked(new Pair<>(stack, context));
		} catch (Exception e) {
			return new EmptyBakedModel();
		}
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
			return registry.find(composite, context);
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
