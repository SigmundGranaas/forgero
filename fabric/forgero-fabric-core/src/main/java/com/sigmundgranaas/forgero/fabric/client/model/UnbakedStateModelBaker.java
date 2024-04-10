package com.sigmundgranaas.forgero.fabric.client.model;

import static com.sigmundgranaas.forgero.minecraft.common.client.forgerotool.model.implementation.EmptyBakedModel.EMPTY;

import java.util.Optional;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.model.ModelRegistry;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
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
	private final Identifier model;
	private final ModelRegistry registry;
	private final StateService service;

	public UnbakedStateModelBaker(Identifier model, ModelRegistry registry, StateService service) {
		this.model = model;
		this.registry = registry;
		this.service = service;
	}

	@Nullable
	@Override
	public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		StateModelBaker modelBaker = new StateModelBaker(baker, textureGetter, registry);
		Optional<State> state = service.find(model);
		ModelStrategy strategy = new CachedSingleStateStrategy(new BlockingModelStrategy(modelBaker), 1000 * 60);
		Optional<BakedModel> model = state
				.flatMap(s -> modelBaker.bake(s, MatchContext.of()))
				.map(m -> new BakedDefaultedStateModel(m.model()))
				.map(m -> new DefaultedDynamicBakedModel(m, strategy, service))
				.map(QuadProviderPreparer::new);

		return model.orElse(EMPTY);

	}
}
