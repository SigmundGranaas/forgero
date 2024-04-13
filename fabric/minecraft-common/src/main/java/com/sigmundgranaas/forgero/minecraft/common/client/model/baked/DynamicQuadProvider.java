package com.sigmundgranaas.forgero.minecraft.common.client.model.baked;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DynamicQuadProvider {
	List<BakedQuad> getQuads(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed, @Nullable Direction face, Random random);

	BakedModel getModel(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed);
}
