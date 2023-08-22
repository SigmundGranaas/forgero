package com.sigmundgranaas.forgero.core.resource.data.v2.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import lombok.Builder;

@Builder(toBuilder = true)
public class DependencyData {

	@Builder.Default
	@SerializedName(value = "dependencies", alternate = "dependency")
	private Set<String> dependencies = Collections.emptySet();

	@Builder.Default
	@SerializedName(value = "any", alternate = "any_of")
	private Set<String> any_of = Collections.emptySet();

	@Builder.Default
	@SerializedName(value = "none", alternate = "none_of")
	private Set<String> none_of = Collections.emptySet();

	public static DependencyData of(List<String> dependencies) {
		return DependencyData.builder().dependencies(Set.copyOf(dependencies)).build();
	}

	public static DependencyData empty() {
		return DependencyData.builder().build();
	}

	public Set<String> getDependencies() {
		return safe(dependencies);
	}

	private Set<String> safe(@Nullable Set<String> elements) {
		return Objects.requireNonNullElseGet(elements, Collections::emptySet);
	}


	public Set<String> getAny_of() {
		return safe(any_of);
	}

	public Set<String> getNone_of() {
		return safe(none_of);
	}

	public boolean isEmpty() {
		return getDependencies().isEmpty() && getAny_of().isEmpty() && getNone_of().isEmpty();
	}

	public static class DependencyDataDeserializer implements JsonDeserializer<DependencyData> {

		@Override
		public DependencyData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (json.isJsonArray()) {
				// Handle as array
				List<String> dependenciesList = new ArrayList<>();
				for (JsonElement element : json.getAsJsonArray()) {
					dependenciesList.add(element.getAsString());
				}
				return DependencyData.of(dependenciesList);
			} else if (json.isJsonObject()) {
				// Handle as object
				return new Gson().fromJson(json, DependencyData.class);
			}
			throw new JsonParseException("Unsupported type for dependencies field");
		}
	}
}
