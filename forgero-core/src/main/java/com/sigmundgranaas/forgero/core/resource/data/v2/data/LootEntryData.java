package com.sigmundgranaas.forgero.core.resource.data.v2.data;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter()
@Accessors(fluent = true)
@Builder(toBuilder = true)
public class LootEntryData {
	@Builder.Default
	private String id = EMPTY_IDENTIFIER;

	@Builder.Default
	@SerializedName(value = "targets", alternate = {"target_tables", "tables"})
	private List<String> targets = Collections.emptyList();

	@Builder.Default
	private List<String> types = Collections.emptyList();

	@Builder.Default
	private List<String> ids = Collections.emptyList();

	@Builder.Default
	private int priority = 0;
	@Builder.Default
	private int upper_rarity_limit = 10;
	@Builder.Default
	private int lower_rarity_limit = 0;
	@Builder.Default
	private float chance = 1f;
	@Builder.Default
	private int rolls = 1;

	public List<String> ids() {
		return Objects.requireNonNullElse(ids, Collections.emptyList());
	}

	public List<String> types() {
		return Objects.requireNonNullElse(types, Collections.emptyList());
	}
}
