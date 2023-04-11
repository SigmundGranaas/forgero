package com.sigmundgranaas.forgero.core.resource.data.v2.data;


import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;
import lombok.Builder;
import lombok.Getter;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

@Getter
@Builder(toBuilder = true)
public class SoulLevelPropertyData {
	@Builder.Default
	@SerializedName(value = "id", alternate = {"entity"})
	private String id = EMPTY_IDENTIFIER;
	@Builder.Default
	private int priority = 0;

	@Builder.Default
	private PropertyPojo properties = new PropertyPojo();
}
