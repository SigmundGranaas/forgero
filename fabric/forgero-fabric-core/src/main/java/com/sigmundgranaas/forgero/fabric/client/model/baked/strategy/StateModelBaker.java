package com.sigmundgranaas.forgero.fabric.client.model.baked.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.model.CompositeModelTemplate;
import com.sigmundgranaas.forgero.core.model.ModelRegistry;
import com.sigmundgranaas.forgero.core.model.ModelResult;
import com.sigmundgranaas.forgero.core.model.ModelTemplate;
import com.sigmundgranaas.forgero.core.model.PaletteTemplateModel;
import com.sigmundgranaas.forgero.core.model.TextureBasedModel;
import com.sigmundgranaas.forgero.core.model.TextureModel;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.BakedModelResult;
import com.sigmundgranaas.forgero.minecraft.common.client.model.Unbaked2DTexturedModel;
import com.sigmundgranaas.forgero.minecraft.common.client.model.UnbakedDynamicModel;

import net.minecraft.client.render.model.Baker;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

public class StateModelBaker implements ModelStrategy {
	private final Baker baker;
	private final Function<SpriteIdentifier, Sprite> textureGetter;
	private final ModelRegistry registry;

	public StateModelBaker(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelRegistry registry) {
		this.baker = baker;
		this.textureGetter = textureGetter;
		this.registry = registry;
	}

	public Optional<BakedModelResult> bake(State state, MatchContext context) {
		return registry.find(state, MatchContext.mutable(context))
				.flatMap(this::convertModel);
	}

	private Optional<BakedModelResult> convertModel(ModelResult template) {
		var unbakedModel = template.getTemplate()
				.convert(this::modelConverter);
		if (unbakedModel.isPresent()) {
			return unbakedModel.map(UnbakedDynamicModel::bake)
					.map(baked -> new BakedModelResult(template, baked));
		}
		return Optional.empty();
	}

	private Optional<UnbakedDynamicModel> modelConverter(ModelTemplate input) {
		var textureList = new ArrayList<ModelTemplate>();
		if (input instanceof CompositeModelTemplate model) {
			model.getModels().forEach(template -> textureCollector(template, textureList));
			var unbakedModel = new Unbaked2DTexturedModel(baker, textureGetter, textureList, "dummy");
			return Optional.of(unbakedModel);
		} else if (input instanceof TextureBasedModel model) {
			textureCollector(model, textureList);
			var unbakedModel = new Unbaked2DTexturedModel(baker, textureGetter, textureList, model.getTexture());
			return Optional.of(unbakedModel);
		}
		return Optional.empty();
	}

	private void textureCollector(ModelTemplate template, List<ModelTemplate> accumulator) {
		if (template instanceof PaletteTemplateModel palette) {
			textureCollector(palette, accumulator);
		} else if (template instanceof CompositeModelTemplate composite) {
			textureCollector(composite, accumulator);
		} else if (template instanceof TextureModel textureModel) {
			textureCollector(textureModel, accumulator);
		}
	}

	private void textureCollector(PaletteTemplateModel template, List<ModelTemplate> accumulator) {
		accumulator.add(template);
		template.children().forEach(child -> textureCollector(child, accumulator));
	}

	private void textureCollector(CompositeModelTemplate template, List<ModelTemplate> accumulator) {
		template.getModels().forEach(model -> textureCollector(model, accumulator));
	}

	private void textureCollector(TextureModel template, List<ModelTemplate> accumulator) {
		accumulator.add(template);
		template.children().forEach(child -> textureCollector(child, accumulator));
	}

	@Override
	public Optional<BakedModelResult> getModel(State state, MatchContext context) {
		return bake(state, context);
	}
}
