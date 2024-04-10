package com.sigmundgranaas.forgero.fabric.client.model;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.match.ItemWorldEntityKey;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class DefaultedDynamicBakedModel implements DynamicQuadProvider {
	private final BakedModel defaultModel;
	private final ModelStrategy strategy;
	private final StateService service;

	public DefaultedDynamicBakedModel(BakedModel defaultModel, ModelStrategy strategy, StateService service) {
		this.defaultModel = defaultModel;
		this.strategy = strategy;
		this.service = service;
	}

	@Override
	public List<BakedQuad> getQuads(ItemStack stack, @Nullable World world, @Nullable Entity entity, @Nullable Direction face, Random random) {
		if (stack.hasNbt()) {
			Optional<State> state = service.convert(stack);
			if (state.isPresent()) {
				ItemWorldEntityKey key = new ItemWorldEntityKey(stack, world, entity);
				MatchContext ctx = new MatchContext()
						.put(ENTITY, key.entity())
						.put(WORLD, key.world())
						.put(STACK, key.stack());
				return strategy.getModel(state.get(), ctx).model().getQuads(null, face, random);
			}
		}
		return defaultModel.getQuads(null, face, random);
	}
}
