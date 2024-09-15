package com.sigmundgranaas.forgero.core.resource.data.v2.data;


import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class ConditionData {
	@Builder.Default
	private String id = EMPTY_IDENTIFIER;
	@Builder.Default
	private int priority = 0;
	@Builder.Default
	private Target target = Target.EMPTY;
	@Builder.Default
	private float chance = 0.01f;
	@Builder.Default
	private PropertyPojo properties = new PropertyPojo();
	@Builder.Default
	@SerializedName("custom_data")
	private Map<String, JsonElement> customData = new HashMap<>();

	@Getter
	@Builder(toBuilder = true)
	public static class Target {
		@Builder.Default
		public static Target EMPTY = Target.builder().build();
		@Builder.Default
		private Set<String> types = Collections.emptySet();
		@Builder.Default
		private Set<String> ids = Collections.emptySet();
		@Builder.Default
		private List<String> incompatibilities = Collections.emptyList();
	}
}
