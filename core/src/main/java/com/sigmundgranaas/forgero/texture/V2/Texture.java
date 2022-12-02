package com.sigmundgranaas.forgero.texture.V2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public interface Texture {
    InputStream getStream() throws IOException;

    BufferedImage getImage();

    static InputStream imageToStream(BufferedImage image) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        os.close();
        return is;
    }
}
