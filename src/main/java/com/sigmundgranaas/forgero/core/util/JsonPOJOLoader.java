package com.sigmundgranaas.forgero.core.util;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.utils.Utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public class JsonPOJOLoader {
    /**
     * Method for loading data from a JSON file and converting it into POJO class T
     *
     * @return Optional<T> POJO
     */
    public static <T> Optional<T> loadPOJO(String filePath, Class<T> type) {
        InputStream materialsStream = Utils.readJsonResourceAsString(filePath);
        if (materialsStream != null) {
            JsonReader materialsJson = new JsonReader(new InputStreamReader(materialsStream));
            return Optional.of(new Gson().fromJson(materialsJson, type));
        } else {
            ForgeroInitializer.LOGGER.error("Unable to load: {}", filePath);
            return Optional.empty();
        }
    }
}
