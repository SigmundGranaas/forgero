package com.sigmundgranaas.forgero.fabric.client.model;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class BakedDefaultedStateModel implements ItemModelWrapper, DynamicQuadProvider {
	private final BakedModel defaultModel;

	public BakedDefaultedStateModel(BakedModel defaultModel) {
		this.defaultModel = defaultModel;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		return defaultModel.getQuads(state, face, random);
	}

	@Override
	public Sprite getParticleSprite() {
		return defaultModel.getParticleSprite();
	}

	@Override
	public List<BakedQuad> getQuads(ItemStack stack, @Nullable World world, @Nullable Entity entity, @Nullable Direction face, Random random) {
		if (isDefault(stack)) {
			return defaultModel.getQuads(null, face, random);
		}
		return Collections.emptyList();
	}

	private boolean isDefault(ItemStack stack) {
		return !stack.hasNbt();
	}
}
