package com.sigmundgranaas.forgero.minecraft.common.client.model.baked;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public interface ItemModelWrapper extends BakedModel {
	Identifier HANDHELD = new Identifier("minecraft:models/item/handheld");

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

	@Override
	default Sprite getParticleSprite() {
		return MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(new Identifier("block/cobblestone"));
	}

	@Override
	default ModelTransformation getTransformation() {
		return DefaultTransformation.loadTransformFromJson(HANDHELD);
	}

	@Override
	default ModelOverrideList getOverrides() {
		return ModelOverrideList.EMPTY;
	}
}
