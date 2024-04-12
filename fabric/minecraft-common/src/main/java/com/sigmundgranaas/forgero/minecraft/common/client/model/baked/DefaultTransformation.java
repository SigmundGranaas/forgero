package com.sigmundgranaas.forgero.minecraft.common.client.model.baked;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Optional;

import com.google.common.base.Charsets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class DefaultTransformation {
	private static final HashMap<Identifier, ModelTransformation> transform = new HashMap<>();

	public static ModelTransformation loadTransformFromJson(Identifier location) {
		if (!transform.containsKey(location)) {
			try {
				ModelTransformation transformations = JsonUnbakedModel.deserialize(getReaderForResource(location)).getTransformations();
				if (transformations != null) {
					transform.put(location, transformations);
				} else {
					return ModelTransformation.NONE;
				}
			} catch (IOException exception) {
				exception.printStackTrace();
				return ModelTransformation.NONE;
			}
		}
		return transform.get(location);
	}

	public static Reader getReaderForResource(Identifier location) throws IOException {
		Identifier file = new Identifier(location.getNamespace(), location.getPath() + ".json");
		Optional<Resource> resource = MinecraftClient.getInstance().getResourceManager().getResource(file);
		return new BufferedReader(new InputStreamReader(resource.get().getInputStream(), Charsets.UTF_8));
	}

	public static void unload() {
		transform.clear();
	}
}
