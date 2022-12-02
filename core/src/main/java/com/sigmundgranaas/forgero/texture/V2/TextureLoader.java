package com.sigmundgranaas.forgero.texture.V2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class TextureLoader {
    private final FileLoader loader;

    public TextureLoader(FileLoader loader) {
        this.loader = loader;
    }

    Optional<BufferedImage> load(String location) {
        var streamOpt = loader.getStream(location);
        if (streamOpt.isPresent()) {
            var stream = streamOpt.get();
            try (stream) {
                return Optional.of(ImageIO.read(stream));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
