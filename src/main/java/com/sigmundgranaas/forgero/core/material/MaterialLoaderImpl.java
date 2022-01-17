package com.sigmundgranaas.forgero.core.material;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.core.exception.NoMaterialsException;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.MaterialPOJO;
import com.sigmundgranaas.forgero.core.material.material.factory.MaterialFactory;
import com.sigmundgranaas.forgero.utils.Utils;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public record MaterialLoaderImpl(String materialPath) implements MaterialLoader {
    public static final HashMap<String, ForgeroMaterial> materialMap = new HashMap<>();
    public static final Logger LOGGER = Forgero.LOGGER;
    private static MaterialLoaderImpl INSTANCE;

    public static MaterialLoader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MaterialLoaderImpl("/config/materials.json");
        }
        return INSTANCE;
    }

    /**
     * @return List<MaterialPOJO>
     * @throws NoMaterialsException - if the method cannot retrieve any materials
     */
    public List<MaterialPOJO> loadMaterials() {
        try {
            InputStream materialsStream = Utils.readJsonResourceAsString(materialPath);

            assert materialsStream != null;

            JsonReader materialsJson = new JsonReader(new InputStreamReader(materialsStream));
            MaterialPOJO[] materials = new Gson().fromJson(materialsJson, MaterialPOJO[].class);
            return Arrays.stream(materials).toList();
        } catch (NullPointerException | JsonIOException e) {
            LOGGER.error("Unable to read Materials from: {}", materialPath);
            LOGGER.error(e);
            throw new NoMaterialsException();
        }
    }

    @Override
    public Map<String, ForgeroMaterial> getMaterials() {
        if (materialMap.isEmpty()) {
            List<MaterialPOJO> jsonMaterials = loadMaterials();
            jsonMaterials.forEach(material -> materialMap.put(material.name.toLowerCase(Locale.ROOT), MaterialFactory.INSTANCE.createMaterial(material)));
        }
        return materialMap;
    }
}
