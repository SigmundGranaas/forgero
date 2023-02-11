package com.sigmundgranaas.forgero.fabric.resources;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.identifier.texture.TemplateIdentifier;
import com.sigmundgranaas.forgero.core.identifier.texture.TextureIdentifier;
import com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier;
import com.sigmundgranaas.forgero.core.texture.V2.FileLoader;

import java.io.InputStream;
import java.util.Optional;

public class FileService implements FileLoader {

    public static final String ASSETS_FOLDER_PATH = "assets/forgero/";
    public static final String TEXTURE_FOLDER_PATH = ASSETS_FOLDER_PATH + "textures/";
    public static String ITEM_TEXTURES_FOLDER_PATH = TEXTURE_FOLDER_PATH + "item/";
    public static String CONFIG_FOLDER_PATH = "config/";


    private String getTexturePath(TextureIdentifier id) {
        if (id.getFileNameWithoutExtension().contains(":")) {
            return "assets/" + id.getFileNameWithExtension().replace(":", "/");
        }
        if (id instanceof TemplateIdentifier texture) {
            return "assets/forgero/templates/textures/" + texture.getFileNameWithExtension();
        } else if (id instanceof PaletteIdentifier) {
            return "assets/forgero/templates/materials/" + id.getFileNameWithExtension();
        } else {
            return "";
        }
    }

    public InputStream getStream(TextureIdentifier id) {
        var inputStream = new FabricModFileLoader().loadFileFromMods(getTexturePath(id));
        if (inputStream.isEmpty()) {
            throw new IllegalArgumentException("file not found! " + getTexturePath(id));
        } else {
            return inputStream.get();
        }
    }

    public Optional<InputStream> getStream(String location) {
        return new FabricModFileLoader().loadFileFromMods(location);
    }

    public Optional<InputStream> getStreamLogged(String location) {
        var inputStream = new FabricModFileLoader().loadFileFromMods(location);
        if (inputStream.isEmpty()) {
            Forgero.LOGGER.error("file not found: {} ", location);
        }
        return inputStream;
    }
}
