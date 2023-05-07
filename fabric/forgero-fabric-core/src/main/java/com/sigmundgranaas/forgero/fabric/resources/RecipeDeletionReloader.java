package com.sigmundgranaas.forgero.fabric.resources;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.core.Forgero;
import lombok.Data;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class RecipeDeletionReloader {

	public static final List<Identifier> entries = new ArrayList<>();


	public static void reload(ResourceManager manager) {
		Gson gson = new Gson();
		entries.clear();
		for (Identifier res : manager.findResources("deleted_recipes", path -> path.endsWith(".json"))) {
			try (InputStream stream = manager.getResource(res).getInputStream()) {
				DeletionData data = gson.fromJson(new JsonReader(new InputStreamReader(stream)), DeletionData.class);
				data.getIds().stream().map(Identifier::new).forEach(entries::add);
			} catch (Exception e) {
				Forgero.LOGGER.error(e);
			}
		}
	}

	@Data
	public static class DeletionData {
		private List<String> ids;
	}
}
