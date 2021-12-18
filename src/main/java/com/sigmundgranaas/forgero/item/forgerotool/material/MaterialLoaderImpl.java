package com.sigmundgranaas.forgero.item.forgerotool.material;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.exception.NoMaterialsException;
import com.sigmundgranaas.forgero.item.forgerotool.material.material.Material;
import com.sigmundgranaas.forgero.item.forgerotool.material.material.MaterialPOJO;
import com.sigmundgranaas.forgero.item.forgerotool.material.material.factory.MaterialFactory;
import com.sigmundgranaas.forgero.utils.Utils;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialLoaderImpl implements MaterialLoader {
    public static final HashMap<String, Material> materialMap = new HashMap<>();
    public static final Logger LOGGER = Forgero.LOGGER;
    private static MaterialLoaderImpl INSTANCE;
    private final String materialPath;

    public MaterialLoaderImpl(String materialPath) {
        this.materialPath = materialPath;
    }

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
    public Map<String, Material> getMaterials() {
        if (materialMap.isEmpty()) {
            List<MaterialPOJO> jsonMaterials = loadMaterials();
            jsonMaterials.forEach(material -> materialMap.put(material.name, MaterialFactory.INSTANCE.createMaterial(material)));
        }
        return materialMap;
    }
}
