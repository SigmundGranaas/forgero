package com.sigmundgranaas.forgero.fabric.client.model;

import static com.sigmundgranaas.forgero.minecraft.common.client.forgerotool.model.implementation.EmptyBakedModel.EMPTY;

import java.util.Optional;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.model.ModelRegistry;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.fabric.client.model.baked.strategy.BakedDefaultedStateModel;
import com.sigmundgranaas.forgero.fabric.client.model.baked.strategy.BlockingModelStrategy;
import com.sigmundgranaas.forgero.fabric.client.model.baked.strategy.LayeredCachedSingleStateStrategy;
import com.sigmundgranaas.forgero.fabric.client.model.baked.strategy.ModelStrategy;
import com.sigmundgranaas.forgero.fabric.client.model.baked.strategy.SingleCachedStateStrategy;
import com.sigmundgranaas.forgero.minecraft.common.client.ForgeroCustomModelProvider;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

public class UnbakedStateModelBaker extends ForgeroCustomModelProvider {
	private final Identifier modelId;
	private final ModelRegistry registry;
	private final StateService service;

	public UnbakedStateModelBaker(Identifier model, ModelRegistry registry, StateService service) {
		this.modelId = model;
		this.registry = registry;
		this.service = service;
	}

	@Nullable
	@Override
	public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		StateModelBaker modelBaker = new StateModelBaker(baker, textureGetter, registry);
		ModelStrategy strategy = new SingleCachedStateStrategy(new LayeredCachedSingleStateStrategy(new BlockingModelStrategy(modelBaker)));
		Optional<BakedModel> model = service.find(this.modelId)
				.flatMap(state -> modelBaker.bake(state, MatchContext.of()))
				.map(baked -> new BakedDefaultedStateModel(baked.model()))
				.map(defaulted -> new DefaultedDynamicBakedModel(defaulted, strategy, service))
				.map(QuadProviderPreparer::new);

		return model.orElse(EMPTY);
	}
}
