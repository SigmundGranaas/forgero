package com.sigmundgranaas.forgero.armor.client.model;

import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.model.registry.api.ModelRegistry;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A baked model that acts as a proxy for dynamically rendered Forgero items.
 * It pre-bakes a default model for its baseline component and uses a custom
 * ModelOverrideList to provide models for dynamically altered ItemStacks.
 */
public class BakedForgeroModel implements BakedModel {
	private final ForgeroItemModelOverrides overrides;
	private final BakedModel defaultBakedModel;
	private final ModelTransformation transformation;

	public BakedForgeroModel(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings settings, Identifier modelId, Component baseline, Function<ItemStack, Optional<Component>> itemToComponent, ModelRegistry modelRegistry) {
		// Load and bake vanilla parent models to get their transformations
		UnbakedModel generatedUnbaked = baker.getOrLoadModel(new Identifier("minecraft:item/generated"));
		BakedModel generatedBaked = generatedUnbaked.bake(baker, textureGetter, settings, new Identifier("forgero:temp_generated_model"));
		ModelTransformation generatedTransform = generatedBaked.getTransformation();

		UnbakedModel handheldUnbaked = baker.getOrLoadModel(new Identifier("minecraft:item/handheld"));
		BakedModel handheldBaked = handheldUnbaked.bake(baker, textureGetter, settings, new Identifier("forgero:temp_handheld_model"));
		ModelTransformation handheldTransform = handheldBaked.getTransformation();

		// Setup our own renderer with the extracted transformations
		var resolver = new RecursiveModelResolver(modelRegistry);
		var renderer = new ForgeroModelRenderer(textureGetter, settings, resolver, modelId, generatedTransform, handheldTransform);

		// Pre-bake the model for the default state of the component.
		this.defaultBakedModel = renderer.bake(baseline);
		this.transformation = this.defaultBakedModel != null ? this.defaultBakedModel.getTransformation() : ModelTransformation.NONE;

		Function<Component, BakedModel> componentBaker = renderer::bake;

		this.overrides = new ForgeroItemModelOverrides(componentBaker, itemToComponent);
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
		return defaultBakedModel != null && defaultBakedModel.isSideLit();
	}

	@Override
	public boolean isBuiltin() {
		return false;
	}

	@Override
	public Sprite getParticleSprite() {
		return defaultBakedModel != null ? defaultBakedModel.getParticleSprite() : null;
	}

	@Override
	public ModelTransformation getTransformation() {
		return transformation;
	}

	@Override
	public ModelOverrideList getOverrides() {
		return this.overrides;
	}
}
