package com.sigmundgranaas.forgero.render.model.item;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.api.item.Model;
import com.sigmundgranaas.forgero.model.registry.api.item.ItemModelRegistry;
import com.sigmundgranaas.forgero.model.resolution.impl.RecursiveModelResolver;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BakedForgeroModel implements BakedModel {
	private static final Logger LOGGER = LoggerFactory.getLogger(BakedForgeroModel.class);
	private final ForgeroItemModelOverrides overrides;
	private final BakedModel defaultBakedModel;
	private final ModelTransformation transformation;
	private final boolean sideLit;
	private final Sprite particleSprite;

	private record ResolvedBakedProperties(ModelTransformation transformation, boolean sideLit, Sprite particleSprite) {}

	public BakedForgeroModel(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings settings, Identifier modelId, Component baseline, Function<ItemStack, Optional<Component>> itemToComponent, ItemModelRegistry modelRegistry) {
		// Find the Model definition for the baseline component
		var forgeroModelOpt = modelRegistry.find(baseline.id());
		if (forgeroModelOpt.isEmpty()) {
			LOGGER.warn("No model definition found for baseline component {}. This item will not render correctly.", baseline.id());
			this.transformation = ModelTransformation.NONE;
			this.sideLit = false;
			this.particleSprite = null;
			this.defaultBakedModel = null;
			this.overrides = new ForgeroItemModelOverrides((c, state) -> null, itemToComponent);
			return;
		}
		var forgeroModel = forgeroModelOpt.get();

		// Resolve all baked properties at once
		ResolvedBakedProperties props = resolveProperties(forgeroModel, baker, textureGetter, settings);
		this.transformation = props.transformation;
		this.sideLit = props.sideLit;
		this.particleSprite = props.particleSprite;

		// Setup renderer with resolved properties
		var resolver = new RecursiveModelResolver(modelRegistry);
		var renderer = new ForgeroModelRenderer(textureGetter, settings, resolver, modelId, this.transformation);

		// Bake default model and setup overrides
		this.defaultBakedModel = renderer.bake(baseline, this.sideLit, this.particleSprite, Collections.emptyMap());
		BiFunction<Component, Map<String, Object>, BakedModel> componentBaker =
				(component, dynamicState) -> renderer.bake(component, this.sideLit, this.particleSprite, dynamicState);
		this.overrides = new ForgeroItemModelOverrides(componentBaker, itemToComponent);
	}

	private ResolvedBakedProperties resolveProperties(Model forgeroModel, Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings settings) {
		ModelTransformation resolvedTransform = ModelTransformation.NONE;
		boolean resolvedSideLit = false;
		BakedModel parentBakedModel = null;

		// Priority 1: "display" block in the model file
		Optional<JsonElement> displayOpt = forgeroModel.getDisplay();
		if (displayOpt.isPresent()) {
			DataResult<ModelTransformation> result = DisplayCodecs.MODEL_TRANSFORMATION_CODEC.parse(JsonOps.INSTANCE, displayOpt.get());
			if (result.result().isPresent()) {
				resolvedTransform = result.result().get();
			} else if (result.error().isPresent()) {
				LOGGER.warn("Failed to parse 'display' block for model {}: {}", forgeroModel.getIdentifier(), result.error().get().message());
			}
		}

		// Priority 2: "parent" model
		Optional<OpenIdentifier> parentOpt = forgeroModel.getParent();
		if (parentOpt.isPresent()) {
			try {
				Identifier parentId = new Identifier(parentOpt.get().toString());
				UnbakedModel parentUnbaked = baker.getOrLoadModel(parentId);
				parentBakedModel = parentUnbaked.bake(baker, textureGetter, settings, new Identifier("forgero:temp_parent_" + parentId.getPath().replace('/', '_')));
				if (parentBakedModel != null) {
					// Only use parent transform if 'display' was not present
					if (displayOpt.isEmpty()) {
						resolvedTransform = parentBakedModel.getTransformation();
					}
					resolvedSideLit = parentBakedModel.isSideLit();
				}
			} catch (Exception e) {
				LOGGER.warn("Failed to load parent model '{}' for Forgero model {}: {}", parentOpt.get(), forgeroModel.getIdentifier(), e.getMessage());
			}
		}

		// If no parent or display, use a fallback (e.g., item/generated)
		if (displayOpt.isEmpty() && parentOpt.isEmpty()) {
			try {
				UnbakedModel generatedUnbaked = baker.getOrLoadModel(new Identifier("minecraft:item/generated"));
				BakedModel generatedBaked = generatedUnbaked.bake(baker, textureGetter, settings, new Identifier("forgero:temp_fallback_generated"));
				resolvedTransform = generatedBaked.getTransformation();
				resolvedSideLit = generatedBaked.isSideLit();
				parentBakedModel = generatedBaked; // use for particle sprite
			} catch (Exception e) {
				LOGGER.error("Failed to load fallback 'minecraft:item/generated' model. Transformations will be broken.", e);
			}
		}

		Sprite resolvedParticleSprite = parentBakedModel != null ? parentBakedModel.getParticleSprite() : null;

		return new ResolvedBakedProperties(resolvedTransform, resolvedSideLit, resolvedParticleSprite);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		return defaultBakedModel != null ? defaultBakedModel.getQuads(state, face, random) : Collections.emptyList();
	}

	@Override
	public boolean useAmbientOcclusion() {
		return defaultBakedModel != null && defaultBakedModel.useAmbientOcclusion();
	}

	@Override
	public boolean hasDepth() {
		return defaultBakedModel != null && defaultBakedModel.hasDepth();
	}

	@Override
	public boolean isSideLit() {
		return this.sideLit;
	}

	@Override
	public boolean isBuiltin() {
		return false;
	}

	@Override
	public Sprite getParticleSprite() {
		return this.particleSprite;
	}

	@Override
	public ModelTransformation getTransformation() {
		return this.transformation;
	}

	@Override
	public ModelOverrideList getOverrides() {
		return this.overrides;
	}
}
