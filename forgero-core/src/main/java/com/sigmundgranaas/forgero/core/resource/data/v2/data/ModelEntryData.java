package com.sigmundgranaas.forgero.core.resource.data.v2.data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.util.Identifiers;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

@Builder
public class ModelEntryData {
	private List<String> target;

	@SerializedName(value = "template", alternate = "texture")
	private String template;

	@Builder.Default
	@Nullable
	private List<Float> offset = Collections.emptyList();

	@Builder.Default
	@Nullable
	private Integer resolution = 16;

	@Builder.Default
	@Nullable
	private Integer order = 1;

	@NotNull
	public List<Float> getOffset() {
		return Objects.requireNonNullElse(offset, Collections.emptyList());
	}

	@NotNull
	public Integer getResolution() {
		return Objects.requireNonNullElse(resolution, 16);
	}

	@NotNull
	public List<String> getTarget() {
		return target;
	}

	@NotNull
	public String getTemplate() {
		return Objects.requireNonNullElse(template, Identifiers.EMPTY_IDENTIFIER);
	}

	@NotNull
	public Integer getOrder() {
		return Objects.requireNonNullElse(order, 1);
	}
}
