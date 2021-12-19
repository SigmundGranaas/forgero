package com.sigmundgranaas.forgero.material;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;

public class MaterialManager {
    public static final HashMap<String, ForgeroPrimaryMaterial> materialMap = new HashMap<>();
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAME);

    public static void initializePrimaryMaterials() {
        String filePath = "/config/materials.json";
        InputStream materialsStream = Utils.readJsonResourceAsString(filePath);
        assert materialsStream != null;

        JsonReader materialsJson = new JsonReader(new InputStreamReader(materialsStream));
        ForgeroPrimaryMaterial[] materials = new Gson().fromJson(materialsJson, ForgeroPrimaryMaterial[].class);
        Arrays.stream(materials).forEach(material -> materialMap.put(material.getName(), material));
        LOGGER.info("Initialized {} primaryMaterials", materialMap.size());
    }
}
