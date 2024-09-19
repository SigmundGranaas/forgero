package com.sigmundgranaas.forgero.predicate.error;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.sigmundgranaas.forgero.predicate.util.JsonUtil;

public class WarningMessageBuilder {
	public static <R> StringBuilder createBaseWarningMessage(DynamicOps<R> ops, MapLike<R> input) {
		StringBuilder warningMessage = new StringBuilder();
		warningMessage.append("Encountered issues when parsing the following JSON snippet \n");
		warningMessage.append(JsonUtil.prettyPrintJson(ops, input)).append("\n");
		warningMessage.append("Explanation \n");
		return warningMessage;
	}
}

