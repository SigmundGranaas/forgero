package com.sigmundgranaas.forgero.minecraft.common.client.model;

import java.util.Collections;
import java.util.List;

import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.DynamicQuadProvider;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.ItemModelWrapper;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public final class QuadProviderPreparer implements ItemModelWrapper {
	private final DynamicQuadProvider provider;
	private ItemStack stack;
	@Nullable
	private World world;
	@Nullable
	private LivingEntity entity;


	public QuadProviderPreparer(DynamicQuadProvider provider) {
		this.provider = provider;
	}

	public void provideContext(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity) {
		this.stack = stack;
		this.world = world;
		this.entity = entity;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		if (stack != null) {
			return provider.getQuads(stack, world, entity, face, random);
		} else {
			return Collections.emptyList();
		}
	}
}
