package com.sigmundgranaas.forgero.fabric.client.model;

import java.util.List;

import com.sigmundgranaas.forgero.minecraft.common.match.ItemWorldEntityKey;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class QuadProviderPreparer implements ItemModelWrapper {
	private final DynamicQuadProvider provider;
	private ItemWorldEntityKey current;

	public QuadProviderPreparer(DynamicQuadProvider provider) {
		this.provider = provider;
	}

	public void provideContext(ItemStack stack, @Nullable World world, @Nullable LivingEntity entity) {
		current = new ItemWorldEntityKey(stack, world, entity);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		return provider.getQuads(current.stack(), current.world(), current.entity(), face, random);
	}
}
