package com.sigmundgranaas.forgero.dynamicresourcepack.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.dynamicresourcepack.api.resource.DynamicResourcePack;

import com.sigmundgranaas.forgero.dynamicresourcepack.util.resource.json.JsonUtil;

import net.minecraft.resource.AbstractFileResourcePack;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DynamicResourcePackImpl implements DynamicResourcePack {
	private final @NotNull Identifier id;
	private final Map<Identifier, byte[]> resources = new ConcurrentHashMap<>();

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
		return resources.keySet().stream().map(Identifier::getNamespace).collect(Collectors.toSet());
	}

	@Override
	public @Nullable <T> T parseMetadata(ResourceMetadataReader<T> metaReader) {
		if(metaReader.getKey().equals("pack")) {
			JsonObject object = new JsonObject();
			object.addProperty("pack_format", 18);
			object.addProperty("description", "runtime resource pack");
			return metaReader.fromJson(object);
		}
		return null;

	}

	@Override
	public @Nullable InputSupplier<InputStream> openRoot(String... segments) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public @Nullable InputSupplier<InputStream> open(@NotNull ResourceType type, @NotNull Identifier id) {
		if(resources.containsKey(id)) {
			return () -> new ByteArrayInputStream(resources.get(id));
		}
		return  null;
	}

	@Override
	public void findResources(ResourceType type, String namespace, String prefix, ResultConsumer consumer) {
		for(Identifier identifier : resources.keySet()) {
			Supplier<byte[]> supplier =  () -> resources.get(identifier);
			InputSupplier<InputStream> inputSupplier = () -> new ByteArrayInputStream(supplier.get());
			if(identifier.getNamespace().equals(namespace) && identifier.getPath().startsWith(prefix)) {
				consumer.accept(identifier, inputSupplier);
			}
		}
	}

	@Override
	public void put(@NotNull Identifier id, @NotNull JsonArray json) {
		resources.put(id, JsonUtil.getGSON().toJson(json).getBytes());
	}

	@Override
	public void put(@NotNull Identifier id, byte[] bytes) {
		resources.put(id, bytes);
	}
}
