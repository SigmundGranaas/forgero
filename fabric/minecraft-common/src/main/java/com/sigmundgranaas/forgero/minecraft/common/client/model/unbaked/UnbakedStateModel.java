package com.sigmundgranaas.forgero.minecraft.common.client.model.unbaked;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.model.ModelRegistry;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.client.model.QuadProviderPreparer;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.DefaultedDynamicBakedModel;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy.ModelStrategy;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy.StateModelBaker;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy.StrategyFactory;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

public class UnbakedStateModel implements UnbakedModel {
	private final Identifier modelId;
	private final ModelRegistry registry;
	private final StateService service;

	public UnbakedStateModel(Identifier model, ModelRegistry registry, StateService service) {
		this.modelId = model;
		this.registry = registry;
		this.service = service;
	}

	@Nullable
	@Override
	public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		State state = service.find(this.modelId).get();
		StateModelBaker modelBaker = new StateModelBaker(baker, textureGetter, registry);
		ModelStrategy strategy = new StrategyFactory(modelBaker, ForgeroConfigurationLoader.configuration.modelStrategy).build(state);

		return new QuadProviderPreparer(new DefaultedDynamicBakedModel(strategy, service));
	}


	@Override
	public Collection<Identifier> getModelDependencies() {
		return Collections.emptyList();
	}

	@Override
	public void setParents(Function<Identifier, UnbakedModel> modelLoader) {
	}
}
