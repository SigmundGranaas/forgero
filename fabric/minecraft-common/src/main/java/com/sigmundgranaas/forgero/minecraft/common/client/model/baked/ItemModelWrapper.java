package com.sigmundgranaas.forgero.minecraft.common.client.model.baked;

import net.minecraft.client.render.model.BakedModel;

public interface ItemModelWrapper extends BakedModel {
	@Override
	default boolean useAmbientOcclusion() {
		return false;
	}

	@Override
	default boolean hasDepth() {
		return false;
	}

	@Override
	default boolean isSideLit() {
		return false;
	}

	@Override
	default boolean isBuiltin() {
		return false;
	}
}
