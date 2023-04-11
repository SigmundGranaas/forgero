package com.sigmundgranaas.drp.impl.resource.data;

import com.google.gson.JsonObject;
import com.sigmundgranaas.drp.impl.resource.JsonData;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(fluent = true)
@SuppressWarnings("ClassCanBeRecord")
public class Ingredient implements JsonData {
	public static String TAG = "tag";
	public static String ITEM = "item";
	private final String type;
	private final String identifier;

	public static Ingredient tag(String tag) {
		return new Ingredient(TAG, tag);
	}

	public static Ingredient item(String item) {
		return new Ingredient(ITEM, item);
	}

	@Override
	public JsonObject asJson() {
		var json = new JsonObject();
		json.addProperty(type, identifier);
		return json;
	}
}
