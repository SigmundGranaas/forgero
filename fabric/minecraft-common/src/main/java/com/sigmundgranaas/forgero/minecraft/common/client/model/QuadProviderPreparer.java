package com.sigmundgranaas.forgero.minecraft.common.client.model;

import java.util.Collections;
import java.util.List;

import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.DynamicQuadProvider;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.ItemModelWrapper;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public final class QuadProviderPreparer implements ItemModelWrapper {
	private final DynamicQuadProvider provider;
	@Nullable
	private ItemStack stack;
	@Nullable
	private ClientWorld world;
	@Nullable
	private LivingEntity entity;

	private int seed = 1;

	public QuadProviderPreparer(DynamicQuadProvider provider) {
		this.provider = provider;
	}

	private void getContext() {
		RenderContextManager.RenderContext context = RenderContextManager.getCurrentContext();
		this.stack = context.stack();
		this.world = context.world();
		this.entity = context.entity();
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		getContext();
		if (stack != null) {
			return provider.getQuads(stack, world, entity, seed, face, random);
		} else {
			return Collections.emptyList();
		}
	}
}
