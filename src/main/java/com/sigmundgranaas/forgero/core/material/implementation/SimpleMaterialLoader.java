package com.sigmundgranaas.forgero.core.material.implementation;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.core.exception.NoMaterialsException;
import com.sigmundgranaas.forgero.core.material.MaterialLoader;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.factory.MaterialFactory;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleMaterialPOJO;
import com.sigmundgranaas.forgero.utils.Utils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public record SimpleMaterialLoader(String materialPath) implements MaterialLoader {
    public static final HashMap<String, ForgeroMaterial> materialMap = new HashMap<>();
    public static final Logger LOGGER = Forgero.LOGGER;
    private static SimpleMaterialLoader INSTANCE;

    public static MaterialLoader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SimpleMaterialLoader("/config/forgero/materials/simple/");
        }
        return INSTANCE;
    }

    /**
     * @return List<MaterialPOJO>
     * @throws NoMaterialsException - if the method cannot retrieve any materials
     */
    public List<SimpleMaterialPOJO> loadMaterials() {
        try {
            File folder = new File(SimpleMaterialLoader.class.getResource(materialPath).getFile());
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null && listOfFiles.length != 0) {
                return Arrays.stream(listOfFiles).map(file -> loadMaterial(file.getName())).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
            } else {
                throw new NoMaterialsException();
            }

        } catch (NullPointerException | JsonIOException e) {
            LOGGER.error("Unable to read Materials from: {}", materialPath);
            LOGGER.error(e);
            throw new NoMaterialsException();
        }
    }

    /**
     * @return MaterialPOJO
     * @throws NoMaterialsException - if the method cannot retrieve any materials
     */
    public Optional<SimpleMaterialPOJO> loadMaterial(String path) {

        InputStream materialsStream = Utils.readJsonResourceAsString(materialPath + path);

        if (materialsStream != null) {
            JsonReader materialsJson = new JsonReader(new InputStreamReader(materialsStream));
            return Optional.of(new Gson().fromJson(materialsJson, SimpleMaterialPOJO.class));
        } else {
            LOGGER.error("Unable to load: {}", path);
            return Optional.empty();
        }
    }

    @Override
    public Map<String, ForgeroMaterial> getMaterials() {
        if (materialMap.isEmpty()) {
            List<SimpleMaterialPOJO> jsonMaterials = loadMaterials();
            jsonMaterials.forEach(material -> materialMap.put(material.name.toLowerCase(Locale.ROOT), MaterialFactory.INSTANCE.createMaterial(material)));
        }
        return materialMap;
    }
}


