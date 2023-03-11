package com.sigmundgranaas.forgero.core.property.v2.feature;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.Collections;
import java.util.List;

import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.active.BreakingDirection;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder(toBuilder = true)
@Getter
@ToString
public class PropertyData implements Property {
	@Builder.Default
	private int priority = 0;
	@Builder.Default
	private String id = EMPTY_IDENTIFIER;
	@Builder.Default
	private String type = EMPTY_IDENTIFIER;
	@Builder.Default
	private String name = EMPTY_IDENTIFIER;
	@Builder.Default
	private float value = 0f;
	@Builder.Default
	private int level = 0;
	@Builder.Default
	private List<String> tags = Collections.emptyList();
	@Builder.Default
	private BreakingDirection direction = BreakingDirection.ANY;
	@Builder.Default
	private String[] pattern = {};
	@Builder.Default
	private String description = EMPTY_IDENTIFIER;

	@Override
	public String type() {
		return type;
	}

	public boolean is(String type) {
		return this.type.equals(type);
	}
}
