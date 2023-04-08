package com.sigmundgranaas.forgero.minecraft.common.dynamic.resource.data;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.minecraft.common.dynamic.resource.JsonData;
import com.sigmundgranaas.forgero.minecraft.common.utils.RegistryUtils;
import lombok.Data;
import lombok.experimental.Accessors;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

@Data
@Accessors(fluent = true)
public class Result implements JsonData {
	private int count = 1;
	private String id;

	public static Result of(Identifier id) {
		return new Result().id(id.toString());
	}

	public static Result of(Item item) {
		return of(RegistryUtils.convert(item));
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
