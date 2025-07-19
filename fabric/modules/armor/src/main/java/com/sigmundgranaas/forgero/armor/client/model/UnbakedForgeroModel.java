package com.sigmundgranaas.forgero.armor.client.model;

import net.minecraft.client.render.model.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

public class UnbakedForgeroModel implements UnbakedModel {
	@Override
	public Collection<Identifier> getModelDependencies() {
		return Collections.emptyList();
	}

	@Override
	public void setParents(Function<Identifier, UnbakedModel> modelLoader) {
	}

	@Nullable
	@Override
	public BakedModel bake(Baker baker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings settings, Identifier modelId) {
		// Instantiate our baked model, passing along the crucial Baker context.
		return new BakedForgeroModel(baker, textureGetter, settings, modelId);
	}
}
