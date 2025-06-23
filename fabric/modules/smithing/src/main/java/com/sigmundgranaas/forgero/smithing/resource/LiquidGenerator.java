package com.sigmundgranaas.forgero.smithing.resource;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.identifier.texture.TemplateIdentifier;
import com.sigmundgranaas.forgero.core.texture.RawTexture;
import com.sigmundgranaas.forgero.core.texture.palette.Palette;
import com.sigmundgranaas.forgero.core.texture.utils.RgbColour;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class LiquidGenerator {
    /**
     * Applies a palette to a grayscale texture, mapping brightness to palette colors.
     * @param grayscale The grayscale BufferedImage (TYPE_BYTE_GRAY recommended)
     * @param palette The palette to use
     * @return A new BufferedImage with the palette applied
     */
    public static BufferedImage applyPalette(BufferedImage grayscale, Palette palette) {
        int width = grayscale.getWidth();
        int height = grayscale.getHeight();
        BufferedImage colored = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        List<RgbColour> colors = palette.getColourValues();
        int paletteSize = colors.size();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = grayscale.getRGB(x, y) & 0xFF; // Only blue channel for grayscale
                int paletteIndex = (int) ((gray / 255.0) * (paletteSize - 1));
                RgbColour color = colors.get(paletteIndex);
                colored.setRGB(x, y, color.getRgb());
            }
        }
        return colored;
    }

    /**
     * Loads a PNG file as a BufferedImage template.
     * @param filePath Path to the PNG file
     * @return BufferedImage loaded from file
     * @throws IOException if the file cannot be read
     */
    public static BufferedImage loadTemplate(String filePath) throws IOException {
        return ImageIO.read(new File(filePath));
    }

    /**
     * Loads the default fluid flow template from the assets directory.
     * @return BufferedImage loaded from assets/forgero/textures/block/fluid_flow.png
     * @throws IOException if the file cannot be read
     */
    public static BufferedImage loadDefaultFluidFlowTemplate(ResourceManager resourceManager) throws IOException {
        Identifier id = new Identifier("forgero", "textures/block/fluid_flow.png");
        Resource resource = resourceManager.getResource(id).orElseThrow(() -> new IOException("Resource not found: " + id));
        try (InputStream stream = resource.getInputStream()) {
            return ImageIO.read(stream);
        }
    }

    /**
     * Saves a BufferedImage as a PNG file to the assets/forgero/textures/block directory at runtime.
     * @param image The BufferedImage to save
     * @param fileName The name of the output PNG file (e.g., "fluid_flow_variant.png")
     * @throws IOException if the file cannot be written
     */
    public static void saveToAssetsBlock(BufferedImage image, String fileName) throws IOException {
        File outputDir = new File("assets/forgero/textures/block");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File outputFile = new File(outputDir, fileName);
        ImageIO.write(image, "PNG", outputFile);
    }

    private static void saveImage(RawTexture texture, String name) {
        var outputPath = "./debug/generated_textures/";
        File outputDir = new File(outputPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File outputFile = new File(outputDir, name);
        try {
            ImageIO.write(texture.image(), "png", outputFile);
            Forgero.LOGGER.info("exported: {}", outputFile.toString());
        } catch (IOException e) {
            Forgero.LOGGER.error(e);
        }
    }

    /**
     * Automates the generation and export of fluid_flow variants for all provided palettes.
     * Each variant is saved as ./export/generated_textures/fluid_flow_{paletteName}.png if export is enabled.
     * @param palettes Map of palette names to Palette instances
     * @throws IOException if reading or writing files fails
     */
    public static void generateAndExportAllFluidFlowVariants(ResourceManager resourceManager, Map<String, ?> palettes) throws IOException {
        BufferedImage template = loadDefaultFluidFlowTemplate(resourceManager);
        for (Map.Entry<String, ?> entry : palettes.entrySet()) {
            String paletteName = entry.getKey();
            Object paletteObj = entry.getValue();
            if (!(paletteObj instanceof Palette palette)) {
                Forgero.LOGGER.error("Palette for {} is not a valid Palette instance: {}", paletteName, paletteObj);
                continue;
            }
            BufferedImage variant = applyPalette(template, palette);
            if (ForgeroConfigurationLoader.configuration.exportGeneratedTextures) {
                TemplateIdentifier id = new TemplateIdentifier(String.format("fluid_flow_%s", paletteName));
                RawTexture texture = new RawTexture(id, variant);
                saveImage(texture, String.format("fluid_flow_%s.png", paletteName));
            }
        }
    }
}
