package com.sigmundgranaas.forgero.fabric.client.model.baked;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public interface DynamicQuadProvider {
	List<BakedQuad> getQuads(ItemStack stack, @Nullable World world, @Nullable Entity entity, @Nullable Direction face, Random random);
}
