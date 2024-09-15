package com.sigmundgranaas.forgero.client.model.unbaked;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;

public interface UnbakedDynamicModel {
	BakedModel bake();

	ModelIdentifier getId();
}