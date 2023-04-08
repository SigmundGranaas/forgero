package com.sigmundgranaas.forgero.minecraft.common.dynamic.pack;

import static net.minecraft.resource.VanillaDataPackProvider.DEFAULT_PACK_METADATA;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.minecraft.common.dynamic.resource.DynamicResource;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.PackResourceMetadataReader;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;

public class DynamicResourcePack implements ResourcePack {
	private final Map<Identifier, Supplier<Resource>> resources;
	private final String name;

	public DynamicResourcePack(Map<Identifier, Supplier<Resource>> resources, String name) {
		this.resources = resources;
		this.name = name;
	}

	public DynamicResourcePack(String name) {
		this.resources = new HashMap<>();
		this.name = name;
	}

	@Nullable
	@Override
	public InputStream openRoot(String fileName) {
		return null;
	}

	public void addResource(Identifier id, Supplier<Resource> resourceSupplier) {
		if (resources.containsKey(id)) {
			Forgero.LOGGER.warn("Overriding  dynamic resource {} in {}", id, getName());
		}
		resources.put(id, resourceSupplier);
	}

	public void add(DynamicResource res) {
		addResource(res.identifier(), res::resource);
	}

	@Override
	public InputStream open(ResourceType type, Identifier id) throws IOException {
		var optionalResource = Optional.ofNullable(resources.get(id))
				.map(Supplier::get);
		if (optionalResource.isPresent()) {
			return Optional.ofNullable(optionalResource.get().getInputStream())
					.orElseThrow(() -> new IOException(MessageFormat.format("Resource {}, cannot be found in dynamic resource pack {}", id, getName())));
		} else {
			throw new IOException(MessageFormat.format("Resource {}, cannot be found in dynamic resource pack {}", id, getName()));
		}
	}

	@Override
	public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, Predicate<Identifier> allowedPathPredicate) {
		var identifiers = resources.keySet()
				.stream()
				.filter(id -> id.getNamespace().equals(namespace))
				.collect(Collectors.toSet());

		return identifiers.stream()
				.filter(resources::containsKey)
				.filter(id -> id.toString().contains(prefix))
				.collect(Collectors.toList());
	}

	@Override
	public boolean contains(ResourceType type, Identifier id) {
		return resources.containsKey(id);
	}

	@Override
	public Set<String> getNamespaces(ResourceType type) {
		return resources.keySet().stream().map(Identifier::getNamespace).collect(Collectors.toSet());
	}

	@Nullable
	@Override
	public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) {
		var object = new JsonObject();
		var metadata = DEFAULT_PACK_METADATA;
		object.addProperty("description", DEFAULT_PACK_METADATA.getDescription().toString());
		object.addProperty("pack_format", metadata.getPackFormat());
		var root = new JsonObject();
		root.add("pack", object);
		if (metaReader instanceof PackResourceMetadataReader) {
			return metaReader.fromJson(object);
		}
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void close() {

	}
}
