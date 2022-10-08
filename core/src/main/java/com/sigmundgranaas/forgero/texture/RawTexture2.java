package com.sigmundgranaas.forgero.texture;

import com.sigmundgranaas.forgero.identifier.texture.TextureIdentifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public record RawTexture2(TextureIdentifier id,
                          BufferedImage image) implements Texture {

    public static RawTexture2 createRawTexture(TextureIdentifier id, InputStream stream) throws IOException {
        try (stream) {
            BufferedImage image = ImageIO.read(stream);
            return new RawTexture2(id, image);
        }
    }

    @Override
    public InputStream getStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        os.close();
        return is;
    }

    @Override
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public TextureIdentifier getId() {
        return id;
    }
}
