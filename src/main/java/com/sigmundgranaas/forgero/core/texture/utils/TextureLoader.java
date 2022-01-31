package com.sigmundgranaas.forgero.core.texture.utils;

import com.sigmundgranaas.forgero.Forgero;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class TextureLoader {
    public static Optional<BufferedImage> loadTextureFromFile(String path) {
        try {
            File textureFile = getFileFromResource(path);
            BufferedImage texture = ImageIO.read((new BufferedInputStream(new FileInputStream(textureFile))));
            return Optional.of(texture);
        } catch (URISyntaxException | IOException e) {
            //TODO Expand error handling
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static boolean saveTextureToFile(String path, BufferedImage texture) {
        try {
            File targetFile = new File(path).getAbsoluteFile();
            if (targetFile.createNewFile()) {
                return ImageIO.write(texture, "PNG", targetFile);
            } else {
                Forgero.LOGGER.warn("Unable to save texture to path: {}", path);
                return false;
            }
        } catch (IOException e) {
            Forgero.LOGGER.warn("Unable to save texture to path: {}, because: {}", path, e);
            return false;
        }
    }

    public static boolean textureExists(String path) {
        return new File(path).exists();
    }

    public static File getFileFromResource(String path) throws URISyntaxException {
        ClassLoader classLoader = TextureLoader.class.getClassLoader();
        URL resource = classLoader.getResource(path);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + path);
        } else {
            return new File(resource.toURI());
        }
    }
}
