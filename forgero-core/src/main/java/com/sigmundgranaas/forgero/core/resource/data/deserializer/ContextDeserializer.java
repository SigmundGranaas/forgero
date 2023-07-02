package com.sigmundgranaas.forgero.core.resource.data.deserializer;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.sigmundgranaas.forgero.core.context.Context;

public class ContextDeserializer implements JsonDeserializer<Context> {
	@Override
	public Context deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		return Context.of(json.getAsString());
	}
}
