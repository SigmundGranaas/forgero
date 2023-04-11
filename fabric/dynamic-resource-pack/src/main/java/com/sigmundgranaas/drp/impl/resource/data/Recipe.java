package com.sigmundgranaas.drp.impl.resource.data;

import com.google.gson.JsonObject;
import com.sigmundgranaas.drp.impl.resource.JsonData;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Data
@Accessors(fluent = true)
@SuperBuilder
public class Recipe implements JsonData {
	protected String type;
	protected Result result;
	protected String group;

	@Override
	public JsonObject asJson() {
		JsonObject object = new JsonObject();
		object.addProperty("type", type);
		object.add("result", result.asJson());
		return object;
	}
}
