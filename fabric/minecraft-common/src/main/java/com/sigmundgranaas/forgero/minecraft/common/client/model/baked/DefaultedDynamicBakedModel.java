package com.sigmundgranaas.forgero.minecraft.common.client.model.baked;

import static com.sigmundgranaas.forgero.minecraft.common.client.forgerotool.model.implementation.EmptyBakedModel.EMPTY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.api.model.ContextAwareBakedModel;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy.ModelStrategy;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public class DefaultedDynamicBakedModel implements ContextAwareBakedModel, ItemModelWrapper {
	private final ModelStrategy strategy;
	private final StateService service;

	public DefaultedDynamicBakedModel(ModelStrategy strategy, StateService service) {
		this.strategy = strategy;
		this.service = service;
	}

	@Override
	public List<BakedQuad> getQuadsWithContext(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed, @Nullable Direction face, Random random) {
		BakedModel result = getModel(stack, world, entity, seed);
		return result.getQuads(null, face, random);

	}

	public BakedModel getModel(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, int seed) {
		Optional<State> state = service.convert(stack);
		if (state.isPresent()) {
			MatchContext ctx;

			if (stack != null) {
				ctx = MatchContext.of(new MatchContext.KeyValuePair(ENTITY, entity), new MatchContext.KeyValuePair(WORLD, world), new MatchContext.KeyValuePair(STACK, stack));
			} else {
				ctx = MatchContext.of();
			}
			BakedModel model = strategy.getModel(state.get(), ctx)
					.map(BakedModelResult::model)
					.orElse(EMPTY);

			return model.getOverrides().apply(model, stack, world, entity, seed);

		}
		return EMPTY;
	}
}
