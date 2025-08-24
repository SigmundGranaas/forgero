package com.sigmundgranaas.forgero.smithing.item.renderer;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class MorphingBakedModel implements BakedModel {
	private final BakedModel original;
	private final Identifier dynamicTextureId;

	public MorphingBakedModel(BakedModel original, Identifier dynamicTextureId) {
		this.original = original;
		this.dynamicTextureId = dynamicTextureId;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable net.minecraft.block.BlockState state,
									@Nullable Direction face,
									net.minecraft.util.math.random.Random random) {
		// Before getting quads, bind our dynamic texture
		if (dynamicTextureId != null) {
			MinecraftClient.getInstance().getTextureManager().bindTexture(dynamicTextureId);
		}

		// Return the original quads but with our texture bound
		return original.getQuads(state, face, random);
	}

	@Override
	public net.minecraft.client.render.model.json.ModelTransformation getTransformation() {
		return original.getTransformation();
	}

	@Override
	public boolean useAmbientOcclusion() {
		return original.useAmbientOcclusion();
	}

	@Override
	public boolean hasDepth() {
		return original.hasDepth();
	}

	@Override
	public boolean isSideLit() {
		return original.isSideLit();
	}

	@Override
	public boolean isBuiltin() {
		return true; // important, signals DIR is in use
	}

	@Override
	public Sprite getParticleSprite() {
		return original.getParticleSprite();
	}

	@Override
	public ModelOverrideList getOverrides() {
		return ModelOverrideList.EMPTY;
	}
}
