package com.sigmundgranaas.forgero.core.resource.data.v2.data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.util.Identifiers;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

@Builder
public class ModelEntryData {
	@SerializedName(value = "target", alternate = {"predicate", "criteria", "predicates"})
	private List<JsonElement> predicates;
	private String template;
	@Builder.Default
	@Nullable
	private List<Float> offset = Collections.emptyList();

	@Builder.Default
	@Nullable
	private Integer resolution = 16;

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
}
