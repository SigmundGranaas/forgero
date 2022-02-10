package com.sigmundgranaas.forgero.core.gem.implementation;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.core.exception.NoMaterialsException;
import com.sigmundgranaas.forgero.core.material.implementation.SimpleMaterialLoader;
import com.sigmundgranaas.forgero.utils.Utils;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonFolderLoader {

    private String path;

    public JsonFolderLoader(String path) {
        this.path = path;
    }

    /**
     * @return List<MaterialPOJO>
     * @throws NoMaterialsException - if the method cannot retrieve any materials
     */
    public <T> List<T> loadMaterials(Class<T> type) {
        try {
            File folder = new File(SimpleMaterialLoader.class.getResource(path).getFile());
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null && listOfFiles.length != 0) {
                return Arrays.stream(listOfFiles).map(file -> loadMaterial(file.getName(), type)).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
            } else {
                throw new NoMaterialsException();
            }

        } catch (NullPointerException | JsonIOException e) {
            Forgero.LOGGER.error("Unable to read resource from: {}", path);
            Forgero.LOGGER.error(e);
            throw new NoMaterialsException();
        }
    }

    /**
     * @return MaterialPOJO
     * @throws NoMaterialsException - if the method cannot retrieve any materials
     */
    public <T> Optional<T> loadMaterial(String filePath, Class<T> type) {

        InputStream materialsStream = Utils.readJsonResourceAsString(path + filePath);

        if (materialsStream != null) {
            JsonReader materialsJson = new JsonReader(new InputStreamReader(materialsStream));
            return Optional.of(new Gson().fromJson(materialsJson, type));
        } else {
            Forgero.LOGGER.error("Unable to load: {}", filePath);
            return Optional.empty();
        }
    }
}
