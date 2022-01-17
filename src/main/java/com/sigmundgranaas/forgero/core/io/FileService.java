package com.sigmundgranaas.forgero.core.io;

import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.TemplateTextureIdentifier;
import com.sigmundgranaas.forgero.core.texture.utils.TextureLoader;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class FileService {

    public static final String ASSETS_FOLDER_PATH = "assets/forgero/";
    public static final String TEXTURE_FOLDER_PATH = ASSETS_FOLDER_PATH + "textures/";
    public static String ITEM_TEXTURES_FOLDER_PATH = TEXTURE_FOLDER_PATH + "item/";
    public static String CONFIG_FOLDER_PATH = "config/";


    public File getFile(TextureIdentifier id) throws URISyntaxException {
        ClassLoader classLoader = TextureLoader.class.getClassLoader();
        URL resource = classLoader.getResource(getTexturePath(id));
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + getTexturePath(id));
        } else {
            return new File(resource.toURI());
        }
    }

    private String getTexturePath(TextureIdentifier id) {
        if (id instanceof TemplateTextureIdentifier) {
            return "config/forgero/templates/textures/" + id.getFileNameWithExtension();
        } else if (id instanceof PaletteIdentifier) {
            return "config/forgero/templates/materials/" + id.getFileNameWithExtension();
        } else {
            return "";
        }
    }
}
