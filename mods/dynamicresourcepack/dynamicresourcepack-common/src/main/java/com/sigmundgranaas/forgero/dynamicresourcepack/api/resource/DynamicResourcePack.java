package com.sigmundgranaas.forgero.dynamicresourcepack.api.resource;

import com.google.gson.JsonArray;

import net.minecraft.util.Identifier;

import org.jetbrains.annotations.NotNull;

public interface DynamicResourcePack {
	void put(@NotNull Identifier id, @NotNull JsonArray json);
	void put(@NotNull Identifier id, byte[] bytes);
}
