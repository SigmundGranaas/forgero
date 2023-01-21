package com.sigmundgranaas.forgero.core.configuration;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.sigmundgranaas.forgero.core.resource.data.v2.ResourceLocator;
import com.sigmundgranaas.forgero.core.resource.data.v2.loading.PathWalker;
import com.sigmundgranaas.forgero.core.util.loader.ClassLoader;
import com.sigmundgranaas.forgero.core.util.loader.InputStreamLoader;
import com.sigmundgranaas.forgero.core.util.loader.PathFinder;

import java.lang.reflect.Type;
import java.util.Set;

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
				Object value = data.get(key);
				forgeroConfiguration.setByKey(key, value);

				// TODO: Iterate over nested objects
//				if (value instanceof JsonObject)
			});

			return forgeroConfiguration;
		}
	}
}
