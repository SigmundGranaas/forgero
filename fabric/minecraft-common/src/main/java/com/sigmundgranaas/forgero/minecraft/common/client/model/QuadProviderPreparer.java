package com.sigmundgranaas.forgero.minecraft.common.client.model;

import java.util.List;

import com.sigmundgranaas.forgero.minecraft.common.client.api.model.ContextAwareBakedModel;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.DynamicQuadProvider;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.ItemModelWrapper;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public final class QuadProviderPreparer implements ItemModelWrapper, ContextAwareBakedModel {
	private final DynamicQuadProvider provider;

	public QuadProviderPreparer(DynamicQuadProvider provider) {
		this.provider = provider;
	}

	@Override
	public List<BakedQuad> getQuadsWithContext(ItemStack stack, ClientWorld world, LivingEntity entity, int seed, @Nullable Direction face, Random random) {
		return provider.getQuads(stack, world, entity, seed, face, random);
	}
}
