package com.sigmundgranaas.forgero.core.material.implementation;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.exception.NoMaterialsException;
import com.sigmundgranaas.forgero.core.material.MaterialLoader;
import com.sigmundgranaas.forgero.core.material.material.ForgeroMaterial;
import com.sigmundgranaas.forgero.core.material.material.factory.MaterialFactory;
import com.sigmundgranaas.forgero.core.material.material.realistic.RealisticMaterialPOJO;
import com.sigmundgranaas.forgero.utils.Utils;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public record RealisticMaterialLoader(String materialPath) implements MaterialLoader {
    public static final HashMap<String, ForgeroMaterial> materialMap = new HashMap<>();
    public static final Logger LOGGER = ForgeroInitializer.LOGGER;
    private static RealisticMaterialLoader INSTANCE;

    public static MaterialLoader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RealisticMaterialLoader("/data/forgero/materials/realistic/realistic_materials.json");
        }
        return INSTANCE;
    }

    /**
     * @return List<MaterialPOJO>
     * @throws NoMaterialsException - if the method cannot retrieve any materials.json
     */
    public List<RealisticMaterialPOJO> loadMaterials() {
        try {
            InputStream materialsStream = Utils.readJsonResourceAsString(materialPath);

            //assert materialsStream != null;

            JsonReader materialsJson = new JsonReader(new InputStreamReader(materialsStream));
            RealisticMaterialPOJO[] materials = new Gson().fromJson(materialsJson, RealisticMaterialPOJO[].class);
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
            List<RealisticMaterialPOJO> jsonMaterials = loadMaterials();
            jsonMaterials.forEach(material -> materialMap.put(material.name.toLowerCase(Locale.ROOT), MaterialFactory.INSTANCE.createMaterial(material)));
        }
        return materialMap;
    }
}
