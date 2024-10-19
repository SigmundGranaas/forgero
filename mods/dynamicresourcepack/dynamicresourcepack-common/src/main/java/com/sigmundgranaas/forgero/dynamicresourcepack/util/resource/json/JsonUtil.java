package com.sigmundgranaas.forgero.dynamicresourcepack.util.resource.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

public class JsonUtil {
	private static final @NotNull Gson GSON = new GsonBuilder().create();

	public static @NotNull Gson getGSON() {
		return GSON;
	}
}
