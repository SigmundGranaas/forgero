package com.sigmundgranaas.forgero.item.forgerotool.model;

import com.sigmundgranaas.forgero.Forgero;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;

public class Retexture {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);

    public static void main(String[] args) throws IOException, URISyntaxException {


        BufferedInputStream templatePalette = new BufferedInputStream(Objects.requireNonNull(readAsStream("/config/templates/color/template_palette" + ".png")));
        BufferedInputStream palette = new BufferedInputStream(Objects.requireNonNull(readAsStream("/config/templates/color/oak" + ".png")));
        BufferedInputStream template = new BufferedInputStream(Objects.requireNonNull(readAsStream("/config/templates/toolparts/" + "fullhandle_metal_base" + ".png")));
        File texture = new File("src/main/resources/config/" + "oak_fullhandle.png");
        InputStream initialStream = recolor(template, templatePalette, palette, "oak_fullhandle", texture);

        File targetFile = new File("src/main/resources/config/" + "oak_fullhandle.png");
        OutputStream outStream = new FileOutputStream(targetFile);

        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = initialStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        IOUtils.closeQuietly(initialStream);
        IOUtils.closeQuietly(outStream);
    }

    @Nullable
    public static InputStream readAsStream(String path) {
        return Retexture.class.getResourceAsStream(path);
    }

    public static InputStream recolor(BufferedInputStream template, BufferedInputStream templatePalette, BufferedInputStream palette, String textureName, File newFile) throws IOException {


        BufferedImage templateImage;
        BufferedImage paletteImage;
        BufferedImage templatePaletteImage;
        ImageIO.setUseCache(false);
        try {
            templateImage = ImageIO.read(template);
            paletteImage = ImageIO.read(palette);
            templatePaletteImage = ImageIO.read(templatePalette);
        } catch (IOException e) {
            return null;
        }
        try {
            template.close();
            palette.close();
            templatePalette.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Integer> templateColors = new ArrayList<>();
        ArrayList<Integer> paletteColors = new ArrayList<>();
        for (int x = 0; x < templatePaletteImage.getWidth(); ++x) {
            templateColors.add(templatePaletteImage.getRGB(x, 0));
        }
        for (int x = 0; x < paletteImage.getWidth(); ++x) {
            paletteColors.add(paletteImage.getRGB(x, 0));
        }
        for (int y = 0; y < templateImage.getHeight(); ++y) {
            for (int x = 0; x < templateImage.getWidth(); ++x) {
                if (templateImage.getRGB(x, y) != 0) {
                    //LOGGER.info("Base Image: {}", templateImage.getRGB(x, y));
                    for (int i = 0; i < templateColors.size(); ++i) {
                        //LOGGER.info("Template color values: {}", templateColors.get(i));
                        if (templateImage.getRGB(x, y) == templateColors.get(i)) {
                            templateImage.setRGB(x, y, paletteColors.get(i));
                            break;
                        }
                    }
                }


            }
        }


        try {
            if (!newFile.createNewFile()) {
                System.out.println("Failed to create new file");
                ByteArrayOutputStream os = new ByteArrayOutputStream() {
                    @Override
                    public synchronized byte[] toByteArray() {
                        return buf;
                    }
                };
                ImageIO.write(templateImage, "PNG", os);
                return new ByteArrayInputStream(os.toByteArray());
            } else {
                ImageIO.write(templateImage, "PNG", newFile);
                return new FileInputStream(newFile);
            }
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
            LOGGER.warn("Error while creating texture " + textureName);
        }
        return null;
    }
}
