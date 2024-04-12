package com.sigmundgranaas.forgero.minecraft.common.client.model.baked;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class BakedDefaultedStateModel implements ItemModelWrapper, DynamicQuadProvider {
	private final BakedModelResult defaultModel;

	public BakedDefaultedStateModel(BakedModelResult defaultModel) {
		this.defaultModel = defaultModel;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		return defaultModel.model().getQuads(state, face, random);
	}

	@Override
	public Sprite getParticleSprite() {
		return defaultModel.model().getParticleSprite();
	}

	@Override
	public List<BakedQuad> getQuads(ItemStack stack, @Nullable World world, @Nullable Entity entity, @Nullable Direction face, Random random) {
		if (isDefault(stack)) {
			return defaultModel.model().getQuads(null, face, random);
		}
		return Collections.emptyList();
	}

	private boolean isDefault(ItemStack stack) {
		return !stack.hasNbt();
	}
}
