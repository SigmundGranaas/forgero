package com.sigmundgranaas.forgero.core.resource.data.v2.data;

import com.google.gson.*;
import com.sigmundgranaas.forgero.core.resource.data.SchemaVersion;
import com.sigmundgranaas.forgero.core.util.Identifiers;

import java.lang.reflect.Type;

public class DataResourceSerializer implements JsonSerializer<DataResource> {
	private final Gson gson;

	public DataResourceSerializer() {
		this.gson = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(DataResource.class, new DataResourceSerializer())
				.create();
	}

	@Override
	public JsonElement serialize(DataResource resource, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject json = new JsonObject();

		// Add non-empty basic fields
		addIfNotEmpty("name", resource.name(), json);
		addIfNotEmpty("namespace", resource.nameSpace(), json);
		addIfNotEmpty("type", resource.type(), json);
		addIfNotEmpty("parent", resource.parent(), json);

		// Add resource type if not UNDEFINED
		if (resource.resourceType() != ResourceType.UNDEFINED) {
			json.addProperty("resource_type", resource.resourceType().toString());
		}

		// Add version if not V2
		if (resource.version() != SchemaVersion.V2) {
			json.addProperty("version", resource.version().toString());
		}

		// Add priority if not default (5)
		if (resource.priority() != 5) {
			json.addProperty("priority", resource.priority());
		}

		// Add dependencies if not empty
		DependencyData dependencies = resource.dependencies();
		if (!dependencies.isEmpty()) {
			json.add("dependencies", context.serialize(dependencies));
		}

		// Add models if present
		if (!resource.models().isEmpty()) {
			json.add("models", context.serialize(resource.models()));
		}

		// Add construct data if present
		resource.construct().ifPresent(construct ->
				json.add("construct", context.serialize(construct)));

		// Add context data if present
		resource.context().ifPresent(contextData ->
				json.add("context", context.serialize(contextData)));

		// Add host data if present
		resource.container().ifPresent(hostData ->
				json.add("container", context.serialize(hostData)));

		// Add palette data if present
		resource.palette().ifPresent(paletteData ->
				json.add("palette", context.serialize(paletteData)));

		// Add children if not empty
		if (!resource.children().isEmpty()) {
			json.add("children", context.serialize(resource.children()));
		}

		// Add properties if present
		addProperties(resource, json, context);

		return json;
	}

	private void addIfNotEmpty(String propertyName, String value, JsonObject json) {
		if (value != null && !value.equals(Identifiers.EMPTY_IDENTIFIER)) {
			json.addProperty(propertyName, value);
		}
	}

	private void addProperties(DataResource resource, JsonObject json, JsonSerializationContext context) {
		resource.properties().ifPresent(props -> {
			JsonObject properties = new JsonObject();

			// Add attributes if present and not empty
			if (props.getAttributes() != null && !props.getAttributes().isEmpty()) {
				JsonArray attributes = new JsonArray();
				props.getAttributes().stream()
						.filter(attr -> !attr.id.equals(Identifiers.EMPTY_IDENTIFIER))
						.forEach(attr -> {
							JsonObject attrObj = new JsonObject();
							attrObj.addProperty("id", attr.id);
							if (attr.priority != 5) {
								attrObj.addProperty("priority", attr.priority);
							}
							attributes.add(attrObj);
						});
				if (!attributes.isEmpty()) {
					properties.add("attributes", attributes);
				}
			}

			// Add features if present and not empty
			if (props.features != null && !props.features.isEmpty()) {
				properties.add("features", context.serialize(props.features));
			}

			if (!properties.isEmpty()) {
				json.add("properties", properties);
			}
		});
	}

	public String toJson(DataResource resource) {
		return gson.toJson(resource);
	}
}
