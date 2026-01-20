package com.sigmundgranaas.forgero.core.customdata;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.base.Enums;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.TargetTypes;

public class TargetData {
	private final Set<String> targets;

	public TargetData(Set<String> targets) {
		this.targets = targets;
	}

	public static TargetData of(Set<String> targets) {
		return new TargetData(targets);
	}

	public static TargetData of() {
		return new TargetData(Collections.emptySet());
	}

	public static TargetData of(JsonElement element) {
		if (element.isJsonArray()) {
			JsonArray array = element.getAsJsonArray();
			var targets = IntStream.range(0, array.size()).mapToObj(i -> array.getAsString()).collect(Collectors.toSet());
			return new TargetData(targets);
		} else if (element.isJsonObject() && element.getAsJsonObject().has("targets")) {
			return of(element.getAsJsonObject().get("targets"));
		} else {
			return new TargetData(Collections.emptySet());
		}
	}

	public boolean isApplicable(Target target) {
		//empty targets means that the data is always applicable
		if (targets == null || targets.isEmpty()) {
			return true;
		}
		return targets.stream().filter(this::isValid).anyMatch(id -> type(id).isPresent() && target.isApplicable(Set.of(strip(id)), type(id).get()));
	}

	public String strip(String target) {
		return target.split(":")[1];
	}

	public Optional<TargetTypes> type(String target) {
		var optionalType = Enums.getIfPresent(TargetTypes.class, target.split(":")[0]).toJavaUtil();
		return optionalType.or(() -> Enums.getIfPresent(TargetTypes.class, target.split(":")[0].toUpperCase()).toJavaUtil());
	}

	public boolean isValid(String target) {
		return target.split(":").length == 2 && type(target).isPresent();
	}
}
