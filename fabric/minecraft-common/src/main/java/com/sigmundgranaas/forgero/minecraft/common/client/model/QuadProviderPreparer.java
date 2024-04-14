package com.sigmundgranaas.forgero.minecraft.common.client.model;

import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.DynamicQuadProvider;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.ItemModelWrapper;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class QuadProviderPreparer implements ItemModelWrapper {
	private final DynamicQuadProvider provider;
	@Nullable
	private ItemStack stack;
	@Nullable
	private ClientWorld world;
	@Nullable
	private LivingEntity entity;

	private int seed = 0;


	public QuadProviderPreparer(DynamicQuadProvider provider) {
		this.provider = provider;
	}

	public void provideContext(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
		this.stack = stack;
		this.world = world;
		this.entity = entity;
		this.seed = seed;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		if (stack != null) {
			return provider.getQuads(stack, world, entity, seed, face, random);
		} else {
			return Collections.emptyList();
		}
	}
}
