package com.sigmundgranaas.forgero.utils;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.material.MaterialManager;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public class Utils {
    public static String createModelJson(String path, String parent) {
        String[] segments = path.split("/");
        path = segments[segments.length - 1];
        return "{\n" +
                "  \"parent\": \"" + parent + "\",\n" +
                "  \"textures\": {\n" +
                "    \"layer0\": \"" + Forgero.MOD_NAMESPACE + ":item/" + path + "\"\n" +
                "  }\n" +
                "}";
    }

    @Nullable
    public static InputStream readJsonResourceAsString(String path) {
        return MaterialManager.class.getResourceAsStream(path);
    }
}
