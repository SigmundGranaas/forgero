package com.sigmundgranaas.forgero.utils;

import com.sigmundgranaas.forgero.Forgero;
import net.minecraft.util.Identifier;

public class ResourceUtils {
    public static final String ASSETS_FOLDER_PATH = "assets/forgero/";
    public static final String TEXTURE_FOLDER_PATH = ASSETS_FOLDER_PATH + "textures/";
    public static String ITEM_TEXTURES_FOLDER_PATH = TEXTURE_FOLDER_PATH + "item/";
    public static String CONFIG_FOLDER_PATH = "config/";

    public static boolean isForgeroResource(Identifier id) {
        return id.getNamespace().equals(Forgero.MOD_NAMESPACE);
    }

    public static boolean isNotForgeroResource(Identifier id) {
        return !isForgeroResource(id);
    }

    public static boolean isTextureIdentifier(Identifier id) {
        String[] segments = id.getPath().split("/");
        String path = segments[segments.length - 1];
        return path.endsWith(".png") || path.endsWith(".PNG");
    }
}
