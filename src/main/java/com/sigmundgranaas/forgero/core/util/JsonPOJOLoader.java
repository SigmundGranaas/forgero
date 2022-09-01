package com.sigmundgranaas.forgero.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.data.v2.json.JsonContext;
import com.sigmundgranaas.forgero.core.data.v2.json.JsonResource;
import org.jetbrains.annotations.Nullable;

import java.io.File;
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
        InputStream materialsStream = getInputStream(filePath);
        if (materialsStream != null) {
            try {
                JsonReader materialsJson = new JsonReader(new InputStreamReader(materialsStream));
                T gson = new Gson().fromJson(materialsJson, type);
                return Optional.of(gson);
            } catch (JsonSyntaxException e) {
                ForgeroInitializer.LOGGER.error("Unable to parse: {}, check if the file is valid", filePath);
                ForgeroInitializer.LOGGER.error(e);
                return Optional.empty();
            }
        } else {
            ForgeroInitializer.LOGGER.error("Unable to load: {}", filePath);
            return Optional.empty();
        }
    }

    public static Optional<JsonResource> loadPOJOWithContext(String filePath) {
        InputStream materialsStream = getInputStream(filePath);
        if (materialsStream != null) {
            try {
                JsonReader materialsJson = new JsonReader(new InputStreamReader(materialsStream));
                JsonResource gson = new Gson().fromJson(materialsJson, JsonResource.class);
                if (gson != null) {
                    gson.context = createContextFromPath(filePath);
                }
                return Optional.ofNullable(gson);
            } catch (JsonSyntaxException e) {
                ForgeroInitializer.LOGGER.error("Unable to parse: {}, check if the file is valid", filePath);
                ForgeroInitializer.LOGGER.error(e);
                return Optional.empty();
            }
        } else {
            ForgeroInitializer.LOGGER.error("Unable to load: {}", filePath);
            return Optional.empty();
        }
    }

    public static JsonContext createContextFromPath(String filePath) {
        JsonContext context = new JsonContext();
        String[] elements = filePath.split("\\" + File.separator);
        context.fileName = elements[elements.length - 1];
        context.folder = elements[elements.length - 2];
        context.path = filePath.replace(context.fileName, "");
        return context;

    }

    public static <T> Optional<T> loadPOJO(InputStream stream, Class<T> type) {
        if (stream != null) {
            try {
                JsonReader materialsJson = new JsonReader(new InputStreamReader(stream));
                T gson = new Gson().fromJson(materialsJson, type);
                return Optional.of(gson);
            } catch (JsonSyntaxException e) {
                //ForgeroInitializer.LOGGER.error("Unable to parse: {}", filePath);
                return Optional.empty();
            }
        } else {
            //ForgeroInitializer.LOGGER.error("Unable to load: {}", filePath);
            return Optional.empty();
        }
    }

    @Nullable
    public static InputStream getInputStream(String path) {
        return JsonPOJOLoader.class.getResourceAsStream(path);
    }
}
