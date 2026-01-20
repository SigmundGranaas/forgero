package com.sigmundgranaas.forgero.minecraft.common.client.model.baked;

import com.sigmundgranaas.forgero.core.model.ModelResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import net.minecraft.client.render.model.BakedModel;

@Accessors(fluent = true)
@Getter
@Setter
@AllArgsConstructor
public class BakedModelResult {
	private ModelResult result;
	private BakedModel model;
}
