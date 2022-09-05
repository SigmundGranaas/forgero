package com.sigmundgranaas.forgerocore.gem.implementation;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgerocore.Forgero;
import com.sigmundgranaas.forgerocore.exception.NoMaterialsException;
import com.sigmundgranaas.forgerocore.util.Utils;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record JsonFolderLoader(String path) {

    /**
     * @return List<MaterialPOJO>
     * @throws NoMaterialsException - if the method cannot retrieve any materials.json
     */
    public <T> List<T> loadMaterials(Class<T> type) {

        try {
            File folder = new File(JsonFolderLoader.class.getResource(path).getFile());
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null && listOfFiles.length != 0) {
                return Arrays.stream(listOfFiles).map(file -> loadMaterial(file.getName(), type)).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
            } else {
                Forgero.LOGGER.error("Unable to read resource from: {}", path);
                if (listOfFiles == null) {
                    Forgero.LOGGER.error("could not read from resource folder, as it is null");
                    Forgero.LOGGER.error("Does the folder exist: {}", folder.exists());
                    Forgero.LOGGER.error(" {}", folder.getPath());
                }
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
     * @throws NoMaterialsException - if the method cannot retrieve any materials.json
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
