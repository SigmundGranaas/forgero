package com.sigmundgranaas.forgero.fabric.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Charsets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.resource.Resource;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;

public abstract class ForgeroCustomModelProvider implements UnbakedModel, BakedModel, FabricBakedModel {

	public static ModelTransformation loadTransformFromJson(Identifier location) {
		try {
			return JsonUnbakedModel.deserialize(getReaderForResource(location)).getTransformations();
		} catch (IOException exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public static Reader getReaderForResource(Identifier location) throws IOException {
		Identifier file = new Identifier(location.getNamespace(), location.getPath() + ".json");
		Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(file);
		return new BufferedReader(new InputStreamReader(resource.getInputStream(), Charsets.UTF_8));
	}


	@Override
	public ModelTransformation getTransformation() {
		return loadTransformFromJson(new Identifier("minecraft:models/item/handheld"));
	}

	@Override
	public boolean useAmbientOcclusion() {
		return false;
	}

	@Override
	public boolean hasDepth() {
		return false;
	}

	@Override
	public boolean isSideLit() {
		return false;
	}

	@Override
	public boolean isBuiltin() {
		return true;
	}

	@Override
	public Sprite getParticleSprite() {
		return MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(new Identifier("block/cobblestone"));
	}

	@Override
	public ModelOverrideList getOverrides() {
		return ModelOverrideList.EMPTY;
	}

	@Override
	public Collection<Identifier> getModelDependencies() {
		return Collections.emptyList();
	}

	@Override
	public boolean isVanillaAdapter() {
		return true;
	}

}
