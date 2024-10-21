package com.sigmundgranaas.forgero.minecraft.common.client.model.baked;

import static com.sigmundgranaas.forgero.minecraft.common.client.forgerotool.model.implementation.EmptyBakedModel.EMPTY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.ContextKey;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.api.model.ContextAwareBakedModel;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy.ModelStrategy;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.client.render.model.json.ModelTransformationMode;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

public class DefaultedDynamicBakedModel implements ContextAwareBakedModel, ItemModelWrapper {
	public static final ContextKey<ModelTransformationMode> RENDER_MODE = ContextKey.of("render_mode", ModelTransformationMode.class);

	private final ModelStrategy strategy;
	private final StateService service;
	@Nullable
	private BakedModel defaultModel;
	private final ItemStack stack;
	private BakedModel lastUsedModel;

	public DefaultedDynamicBakedModel(ModelStrategy strategy, StateService service, ItemStack stack) {
		this.strategy = strategy;
		this.service = service;
		this.stack = stack;
	}

	@Override
	public List<BakedQuad> defaultQuads(@Nullable Direction face, Random random) {
		return getModel(stack, null, null, null, 0).getQuads(null, face, random);
	}

	@Override
	public List<BakedQuad> getQuadsWithContext(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, ModelTransformationMode mode, int seed, @Nullable Direction face, Random random) {
		BakedModel result = getModel(stack, world, entity, mode, seed);
		return result.getQuads(null, face, random);
	}

	public BakedModel getModel(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity, ModelTransformationMode mode, int seed) {
		ItemStack itemStack = Objects.requireNonNullElse(stack, this.stack);
		Optional<State> state = service.convert(stack);
		if (state.isPresent()) {
			MatchContext ctx;

			ctx = MatchContext.of(new MatchContext.KeyValuePair(ENTITY, entity), new MatchContext.KeyValuePair(WORLD, world), new MatchContext.KeyValuePair(STACK, itemStack), new MatchContext.KeyValuePair(RENDER_MODE, mode));

			BakedModel model = strategy.getModel(state.get(), ctx)
					.map(BakedModelResult::model)
					.or(() -> Optional.ofNullable(defaultModel))
					.orElse(EMPTY);

			this.lastUsedModel = model;
			return model.getOverrides().apply(model, stack, world, entity, seed);
		}
		return EMPTY;
	}

	@Override
	public Sprite getParticleSpriteWithContext(ItemStack stack, ClientWorld world, LivingEntity entity, int seed) {
		return getModel(stack, world, entity, null, seed).getParticleSprite();
	}

	@Override
	public ModelTransformation getTransformationWithContext(ItemStack stack, ClientWorld world, LivingEntity entity, int seed) {
		if(lastUsedModel != null){
			return lastUsedModel.getTransformation();
		}

		if (defaultModel != null) {
			return defaultModel.getTransformation();
		}
		BakedModel model = getModel(stack, world, entity, null, seed);
		if (model != EMPTY && model.getTransformation() != ModelTransformation.NONE) {
			this.defaultModel = model;
			return this.defaultModel.getTransformation();
		}

		return model.getTransformation();
	}

	@Override
	public ModelOverrideList getOverridesWithContext(ItemStack stack, ClientWorld world, LivingEntity entity, int seed) {
		if (defaultModel != null) {
			return defaultModel.getOverrides();
		}
		BakedModel model = getModel(stack, world, entity, null, seed);
		if (model != EMPTY && model.getOverrides() != ModelOverrideList.EMPTY) {
			this.defaultModel = model;
			return this.defaultModel.getOverrides();
		}

		return model.getOverrides();
	}
}

