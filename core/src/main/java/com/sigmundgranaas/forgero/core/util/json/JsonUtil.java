package com.sigmundgranaas.forgero.core.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;

public class JsonUtil {
	protected static final @NotNull Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static @NotNull String prettyPrintJson(@NotNull String json) {
		return GSON.toJson(JsonParser.parseString(json));
	}
}
