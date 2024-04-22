package com.sigmundgranaas.forgero.minecraft.common.client.model.unbaked;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import com.sigmundgranaas.forgero.core.model.ModelRegistry;
import com.sigmundgranaas.forgero.core.model.Strategy;
import com.sigmundgranaas.forgero.core.state.State;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class UnbakedStateModel implements UnbakedModel {
	private final ModelRegistry registry;
	private final StateService service;
	private final Strategy strategy;
	private final State state;

	public UnbakedStateModel(ModelRegistry registry, StateService service, Strategy strategy, State state) {
		this.registry = registry;
		this.service = service;
		this.strategy = strategy;
		this.state = state;
	}

	@Nullable
	@Override
	public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
		StateModelBaker modelBaker = new StateModelBaker(baker, textureGetter, registry);
		ModelStrategy modelStrategy = new StrategyFactory(modelBaker, strategy).build(state);
		return new DefaultedDynamicBakedModel(modelStrategy, service, service.convert(state).orElse(ItemStack.EMPTY));
	}


	@Override
	public Collection<Identifier> getModelDependencies() {
		return Collections.emptyList();
	}

	@Override
	public void setParents(Function<Identifier, UnbakedModel> modelLoader) {
	}
}
