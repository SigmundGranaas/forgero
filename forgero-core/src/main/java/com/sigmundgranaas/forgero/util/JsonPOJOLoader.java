package com.sigmundgranaas.forgero.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.resource.data.v2.data.ContextData;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
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
                return Optional.ofNullable(gson);
            } catch (JsonSyntaxException e) {
                Forgero.LOGGER.error("Unable to parse: {}, check if the file is valid", filePath);
                Forgero.LOGGER.error(e);
                return Optional.empty();
            }
        } else {
            Forgero.LOGGER.error("Unable to load: {}", filePath);
            return Optional.empty();
        }
    }

    public static Optional<DataResource> loadPOJOWithContext(String filePath) {
        InputStream materialsStream = getInputStream(filePath);
        if (materialsStream != null) {
            try {
                JsonReader materialsJson = new JsonReader(new InputStreamReader(materialsStream));
                var context = createContextFromPath(filePath);
                DataResource gson = new Gson().fromJson(materialsJson, DataResource.class);
                if (gson != null) {
                    var resource = gson.toBuilder().context(context).build();
                    if(resource == null){
                        Forgero.LOGGER.error("Unable to load: {}, check if the file is valid", filePath);
                    }
                    return Optional.ofNullable(resource);
                }
                return Optional.empty();
            } catch (JsonSyntaxException e) {
                Forgero.LOGGER.error("Unable to parse: {}, check if the file is valid", filePath);
                Forgero.LOGGER.error(e);
                return Optional.empty();
            }
        } else {
            Forgero.LOGGER.error("Unable to load: {}", filePath);
            return Optional.empty();
        }
    }

    public static ContextData createContextFromPath(String filePath) {
        var builder = ContextData.builder();
        String[] elements = filePath.split("\\" + File.separator);
        if (elements.length == 1) {
            elements = filePath.split("/");
        }
        var fileName = elements[elements.length - 1];
        var folder = elements[elements.length - 2];
        var path = filePath.replace(fileName, "");
        return builder.fileName(fileName).folder(folder).path(path).build();
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
