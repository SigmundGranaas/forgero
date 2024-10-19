package com.sigmundgranaas.forgero.dynamicresourcepack.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.dynamicresourcepack.api.resource.DynamicResourcePack;

import com.sigmundgranaas.forgero.dynamicresourcepack.util.resource.json.JsonUtil;

import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;

import net.minecraft.util.PathUtil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicResourcePackImpl implements ResourcePack, DynamicResourcePack {
	private final @NotNull Identifier id;
	private final Map<Path, byte[]> resources = new ConcurrentHashMap<>();

	public DynamicResourcePackImpl(@NotNull Identifier id) {
		this.id = id;
	}

	@Override
	public void close() {
		// NO-OP
	}

	@Override
	public @NotNull String getName() {
		return id.toString();
	}

	@Override
	public @NotNull Set<String> getNamespaces(ResourceType type) {
		// TODO
		return Set.of();
	}

	@Override
	public @Nullable <T> T parseMetadata(ResourceMetadataReader<T> metaReader) {
		// TODO
		return null;
	}

	@Override
	public @Nullable InputSupplier<InputStream> openRoot(String... segments) {
		@NotNull var path = PathUtil.getPath(Path.of(""), List.of(segments));
		return () -> new ByteArrayInputStream(resources.get(path));
	}

	@Override
	public @Nullable InputSupplier<InputStream> open(@NotNull ResourceType type, @NotNull Identifier id) {
		@NotNull var path = PathUtil.getPath(Path.of(type.getDirectory()), List.of(id.getPath()));
		return () -> new ByteArrayInputStream(resources.get(path));
	}

	@Override
	public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {
		// TODO
	}

	@Override
	public void put(@NotNull Identifier id, @NotNull JsonArray json) {
		resources.put(Path.of(id.getPath()), JsonUtil.getGSON().toJson(json).getBytes());
	}

	@Override
	public void put(@NotNull Identifier id, byte[] bytes) {
		resources.put(Path.of(id.getPath()), bytes);
	}
}
