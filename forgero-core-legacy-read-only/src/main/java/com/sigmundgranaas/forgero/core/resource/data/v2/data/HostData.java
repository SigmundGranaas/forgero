package com.sigmundgranaas.forgero.core.resource.data.v2.data;

import java.util.Objects;

import javax.annotation.Nullable;

import com.sigmundgranaas.forgero.core.util.Identifiers;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

@Builder
public class HostData {
	@Nullable
	@Builder.Default
	private String type = Identifiers.CREATE_IDENTIFIER;
	@Nullable
	@Builder.Default
	private String id = Identifiers.THIS_IDENTIFIER;

	@Nullable
	@Builder.Default
	private String tag = Identifiers.EMPTY_IDENTIFIER;

	@NotNull
	public String getType() {
		return Objects.requireNonNullElse(type, Identifiers.CREATE_IDENTIFIER);
	}

	@NotNull
	public String getId() {
		return Objects.requireNonNullElse(id, Identifiers.EMPTY_IDENTIFIER);
	}

	@NotNull
	public String getTag() {
		return Objects.requireNonNullElse(tag, Identifiers.EMPTY_IDENTIFIER);
	}
}
