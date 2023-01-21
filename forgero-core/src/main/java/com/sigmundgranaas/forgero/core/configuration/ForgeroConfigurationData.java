package com.sigmundgranaas.forgero.core.configuration;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.List;

public interface ForgeroConfigurationData {
	default void setByKey(String key, Object value) {
		try {
			getClass().getField(key).set(this, value);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	default Object getByKey(String key) {
		try {
			return getClass().getField(key).get(this);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	class Deserializer implements JsonDeserializer<ForgeroConfiguration> {
		@Override
		public ForgeroConfiguration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject data = json.getAsJsonObject();
			var forgeroConfiguration = new ForgeroConfiguration();

			data.keySet().forEach(key ->
			{
				JsonElement value = data.get(key);

				if (value.getClass() == JsonArray.class) {
					var valueAsList = new Gson().fromJson(value.toString(), List.class);

					forgeroConfiguration.setByKey(key, valueAsList);
					return;
				}

				if (forgeroConfiguration.getByKey(key).getClass() == Boolean.class) {
					var valueAsBoolean = value.getAsBoolean();

					forgeroConfiguration.setByKey(key, valueAsBoolean);
					return;
				}

				forgeroConfiguration.setByKey(key, value);

				// TODO: Iterate over nested objects
//				if (value instanceof JsonObject)
			});

			return forgeroConfiguration;
		}
	}
}
