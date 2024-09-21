package com.sigmundgranaas.forgero.fabric.initialization.datareloader;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.context.Context;
import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;
import com.sigmundgranaas.forgero.core.resource.data.deserializer.AttributeGroupDeserializer;
import com.sigmundgranaas.forgero.core.resource.data.deserializer.ContextDeserializer;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DependencyData;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;

public class ResourceManagerJsonLoader<T> {

	private final ResourceManager resourceManager;
	private final Class<T> clazz;
	private final String path;

	public ResourceManagerJsonLoader(ResourceManager resourceManager, Class<T> clazz, String path) {
		this.resourceManager = resourceManager;
		this.clazz = clazz;
		this.path = path;
	}

	private Collection<Resource> loadJsonData(String path) {
		return resourceManager.findResources(path, p -> p.getPath().endsWith(".json")).values();
	}

	private Stream<T> streamElements() {
		return loadJsonData(path)
				.stream()
				.map(this::tryParse)
				.flatMap(Optional::stream);
	}

	public List<T> load() {
		return streamElements()
				.collect(Collectors.toList());
	}

	private Optional<T> tryParse(Resource resource) {
		try (var stream = resource.getInputStream()){
			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(DependencyData.class, new DependencyData.DependencyDataDeserializer());
			builder.registerTypeAdapter(new TypeToken<List<PropertyPojo.Attribute>>() {
			}.getType(), new AttributeGroupDeserializer());
			builder.registerTypeAdapter(new TypeToken<Context>() {
			}.getType(), new ContextDeserializer());
			T element = builder.create().fromJson(new JsonReader(new InputStreamReader(stream)), this.clazz);
			return Optional.of(element);
		} catch (Exception e) {
			Forgero.LOGGER.error(e);
			return Optional.empty();
		}
	}
}
