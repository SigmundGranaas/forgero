package com.sigmundgranaas.forgero.utils;

import com.sigmundgranaas.forgero.Forgero;

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
}
