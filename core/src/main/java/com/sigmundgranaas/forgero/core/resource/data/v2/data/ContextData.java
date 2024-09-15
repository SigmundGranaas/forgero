package com.sigmundgranaas.forgero.core.resource.data.v2.data;

import com.sigmundgranaas.forgero.core.util.Identifiers;
import lombok.Builder;

import javax.annotation.Nullable;

@Builder(toBuilder = true)
public class ContextData {
	@Builder.Default
	private final String fileName = Identifiers.EMPTY_IDENTIFIER;

	@Builder.Default
	private final String folder = Identifiers.EMPTY_IDENTIFIER;

	@Builder.Default
	private final String path = Identifiers.EMPTY_IDENTIFIER;

	@Nullable
	private final String parent;

	public String fileName() {
		return fileName;
	}

	public String folder() {
		return folder;
	}

	public String path() {
		return path;
	}

	public String parent() {
		return parent;
	}

}
