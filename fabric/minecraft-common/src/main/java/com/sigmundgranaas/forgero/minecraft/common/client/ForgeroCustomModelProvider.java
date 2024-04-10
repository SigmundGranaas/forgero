package com.sigmundgranaas.forgero.minecraft.common.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.Charsets;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.resource.Resource;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;


public abstract class ForgeroCustomModelProvider implements UnbakedModel, BakedModel {
	private static final HashMap<Identifier, ModelTransformation> transform = new HashMap<>();

	public static ModelTransformation loadTransformFromJson(Identifier location) {
		if (transform.containsKey(location)) {
			try {
				transform.put(location, JsonUnbakedModel.deserialize(getReaderForResource(location)).getTransformations());
			} catch (IOException exception) {
				exception.printStackTrace();
				return null;
			}
		}
		return transform.get(location);
	}

	public static Reader getReaderForResource(Identifier location) throws IOException {
		Identifier file = new Identifier(location.getNamespace(), location.getPath() + ".json");
		Optional<Resource> resource = MinecraftClient.getInstance().getResourceManager().getResource(file);
		return new BufferedReader(new InputStreamReader(resource.get().getInputStream(), Charsets.UTF_8));
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
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		return Collections.emptyList();
	}

	@Override
	public void setParents(Function<Identifier, UnbakedModel> modelLoader) {

	}
}
