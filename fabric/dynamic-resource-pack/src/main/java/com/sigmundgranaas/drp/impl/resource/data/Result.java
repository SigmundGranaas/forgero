package com.sigmundgranaas.drp.impl.resource.data;

import com.google.gson.JsonObject;
import com.sigmundgranaas.drp.impl.resource.JsonData;
import lombok.Data;
import lombok.experimental.Accessors;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Data
@Accessors(fluent = true)
public class Result implements JsonData {
	private int count = 1;
	private String id;

	public static Result of(Identifier id) {
		return new Result().id(id.toString());
	}

	public static Result of(Item item) {
		return of(Registry.ITEM.getId(item));
	}

	public static Result of(String item) {
		return new Result().id(item);
	}

	@Override
	public JsonObject asJson() {
		JsonObject json = new JsonObject();
		json.addProperty("item", id());
		json.addProperty("count", count());
		return json;
	}
}
