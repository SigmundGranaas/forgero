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

@Builder
public class ModelEntryData {
	@SerializedName(value = "target", alternate = {"predicate", "criteria", "predicates"})
	private List<JsonElement> predicates;

	@Nullable
	private String template;

	@Nullable
	private String palette;

	@Builder.Default
	@Nullable
	private List<Float> offset = Collections.emptyList();

	@Builder.Default
	@Nullable
	private Integer resolution = 16;

	@Builder.Default
	@Nullable
	@SerializedName(value = "children", alternate = {"textures"})
	private List<ModelData> children = Collections.emptyList();

	@Builder.Default
	@Nullable
	private String texture =  Identifiers.EMPTY_IDENTIFIER;

	@Nullable
	@SerializedName(value = "display_overrides", alternate = "display")
	private JsonObject displayOverrides;

	@NotNull
	public List<Float> getOffset() {
		return Objects.requireNonNullElse(offset, Collections.emptyList());
	}

	@NotNull
	public Integer getResolution() {
		return Objects.requireNonNullElse(resolution, 16);
	}

	@NotNull
	public List<JsonElement> getTarget() {
		return predicates;
	}

	@NotNull
	public String getTemplate() {
		return Objects.requireNonNullElse(template, Identifiers.EMPTY_IDENTIFIER);
	}

	@NotNull
	public String getPalette() {
		return Objects.requireNonNullElse(palette, Identifiers.EMPTY_IDENTIFIER);
	}

	@NotNull
	public List<ModelData> getChildren() {
		return Objects.requireNonNullElse(children, Collections.emptyList());
	}

	@NotNull
	public String getTexture() {
		return Objects.requireNonNullElse(texture, Identifiers.EMPTY_IDENTIFIER);
	}

	@NotNull
	public Optional<JsonObject> getDisplayOverrides() {
		return Optional.ofNullable(displayOverrides);
	}
}
