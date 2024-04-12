package com.sigmundgranaas.forgero.minecraft.common.client.model.baked;

import static com.sigmundgranaas.forgero.minecraft.common.client.forgerotool.model.implementation.EmptyBakedModel.EMPTY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy.ModelStrategy;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class DefaultedDynamicBakedModel implements DynamicQuadProvider {
	private final ModelStrategy strategy;
	private final StateService service;

	public DefaultedDynamicBakedModel(ModelStrategy strategy, StateService service) {
		this.strategy = strategy;
		this.service = service;
	}

	@Override
	public List<BakedQuad> getQuads(ItemStack stack, @Nullable World world, @Nullable Entity entity, @Nullable Direction face, Random random) {
		Optional<State> state = service.convert(stack);
		if (state.isPresent()) {
			MatchContext ctx;

			if (stack != null) {
				ctx = MatchContext.of(new MatchContext.KeyValuePair(ENTITY, entity), new MatchContext.KeyValuePair(WORLD, world), new MatchContext.KeyValuePair(STACK, stack));
			} else {
				ctx = MatchContext.of();
			}

			return strategy.getModel(state.get(), ctx)
					.map(BakedModelResult::model)
					.orElse(EMPTY)
					.getQuads(null, face, random);
		}

		return Collections.emptyList();
	}
}
