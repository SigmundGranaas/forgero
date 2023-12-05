package com.sigmundgranaas.forgero.core.resource.data.v2.data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.util.Identifiers;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

@Builder(toBuilder = true)
public class ModelData {
	@Builder.Default
	@Nullable
	@SerializedName(value = "target", alternate = {"criteria", "predicate", "predicates"})
	private List<JsonElement> predicate = Collections.emptyList();

	@Builder.Default
	private int order = 0;

	@Builder.Default
	@SerializedName(value = "modelType", alternate = "type")
	@Nullable
	private String modelType = Identifiers.EMPTY_IDENTIFIER;

	@Builder.Default
	@Nullable
	private String template = Identifiers.EMPTY_IDENTIFIER;

	@Builder.Default
	@Nullable
	private String name = Identifiers.EMPTY_IDENTIFIER;

	@Builder.Default
	@Nullable
	private List<ModelEntryData> variants = Collections.emptyList();

	@Builder.Default
	@Nullable
	private List<Float> offset = Collections.emptyList();

	@Builder.Default
	@Nullable
	private String palette = Identifiers.EMPTY_IDENTIFIER;

	@Nullable
	@SerializedName(value = "display_overrides", alternate = "display")
	private JsonObject displayOverrides;

	@Builder.Default
	@Nullable
	private Integer resolution = 16;

	public List<JsonElement> getPredicates() {
		return Objects.requireNonNullElse(predicate, Collections.emptyList());
	}

	public String getModelType() {
		return Objects.requireNonNullElse(modelType, Identifiers.EMPTY_IDENTIFIER);
	}

	public String getTemplate() {
		return Objects.requireNonNullElse(template, Identifiers.EMPTY_IDENTIFIER);
	}

	public String getName() {
		return Objects.requireNonNullElse(name, Identifiers.EMPTY_IDENTIFIER);
	}

	public int order() {
		return order;
	}


	public List<ModelEntryData> getVariants() {
		return Objects.requireNonNullElse(variants, Collections.emptyList());
	}

	public List<Float> getOffset() {
		return Objects.requireNonNullElse(offset, Collections.emptyList());
	}

	public Optional<JsonObject> displayOverrides() {
		return Optional.ofNullable(displayOverrides);
	}

	@NotNull
	public Integer getResolution() {
		return Objects.requireNonNullElse(resolution, 16);

	}

	public String getPalette() {
		return Objects.requireNonNullElse(palette, Identifiers.EMPTY_IDENTIFIER);
	}
}
