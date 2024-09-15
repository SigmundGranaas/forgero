package com.sigmundgranaas.forgero.client.model.baked;


import java.util.List;

import com.sigmundgranaas.forgero.client.forgerotool.model.implementation.EmptyBakedModel;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public class DynamicBakedModel implements BakedModel {
	private volatile BakedModel currentModel;

	public DynamicBakedModel() {
		this.currentModel = EmptyBakedModel.EMPTY;
	}

	public void updateModel(BakedModel newModel) {
		this.currentModel = newModel;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		return currentModel.getQuads(state, face, random);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return currentModel.useAmbientOcclusion();
	}

	@Override
	public boolean hasDepth() {
		return currentModel.hasDepth();
	}

	@Override
	public boolean isSideLit() {
		return currentModel.isSideLit();
	}

	@Override
	public boolean isBuiltin() {
		return currentModel.isBuiltin();
	}

	@Override
	public Sprite getParticleSprite() {
		return currentModel.getParticleSprite();
	}

	@Override
	public ModelTransformation getTransformation() {
		return currentModel.getTransformation();
	}

	@Override
	public ModelOverrideList getOverrides() {
		return currentModel.getOverrides();
	}
}
