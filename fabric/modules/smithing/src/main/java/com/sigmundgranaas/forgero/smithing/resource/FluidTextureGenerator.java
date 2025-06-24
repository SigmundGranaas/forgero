package com.sigmundgranaas.forgero.smithing.resource;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.imageio.ImageIO;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.texture.V2.Palette;
import com.sigmundgranaas.forgero.core.texture.V2.TextureGenerator;
import com.sigmundgranaas.forgero.core.texture.utils.RgbColour;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

public class FluidTextureGenerator implements SimpleSynchronousResourceReloadListener {
    private final TextureGenerator textureGenerator;
    
    public FluidTextureGenerator(TextureGenerator textureGenerator) {
        this.textureGenerator = textureGenerator;
    }
    
    @Override
    public Identifier getFabricId() {
        return new Identifier("forgero", "fluid_texture_generator");
    }
    
    @Override
    public void reload(ResourceManager manager) {
        generateFluidTextures();
    }
    
    public void generateFluidTextures() {
        Map<String, Palette> palettes = textureGenerator.getPaletteMap();
        if (palettes.isEmpty()) {
            return;
        }
        
        // Load the base fluid texture
        String baseTextureName = "fluid";
        BufferedImage baseImage = loadBaseTexture(baseTextureName);
        if (baseImage == null) {
            return;
        }
        
        // Generate palette variants
        int variantCount = 0;
        for (Map.Entry<String, Palette> entry : palettes.entrySet()) {
            String paletteName = entry.getKey();
            // Remove any existing .png extension from palette name
            if (paletteName.endsWith(".png")) {
                paletteName = paletteName.substring(0, paletteName.length() - 4);
            }
            
            Palette palette = entry.getValue();
            
            if (palette.getColourValues(0).isEmpty()) {
                Forgero.LOGGER.warn("Skipping empty palette: {}", paletteName);
                continue;
            }
            
            try {
                BufferedImage variant = createVariant(baseImage, palette);
                String variantName = String.format("%s-fluid", paletteName);
                saveVariant(variant, variantName);
                variantCount++;
            } catch (Exception e) {
                // Failed to create variant
            }
        }
    }
    
    private BufferedImage loadBaseTexture(String name) {
        try {
            // Try to load from the mod resources first
            String resourcePath = String.format("assets/forgero/textures/block/fluid.png");
            try (var stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (stream != null) {
                    return ImageIO.read(stream);
                }
            }
            
            // Fall back to Minecraft resource manager
            Identifier id = new Identifier("forgero", "textures/block/fluid.png");
            var resourceManager = MinecraftClient.getInstance().getResourceManager();
            if (resourceManager != null) {
                var resource = resourceManager.getResource(id).orElse(null);
                if (resource != null) {
                    try (var stream = resource.getInputStream()) {
                        return ImageIO.read(stream);
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
    
    private BufferedImage createVariant(BufferedImage baseImage, Palette palette) {
        int width = baseImage.getWidth();
        int height = baseImage.getHeight();
        BufferedImage variant = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = baseImage.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;
                
                if (alpha == 0) {
                    variant.setRGB(x, y, 0x00000000);
                    continue;
                }
                
                // Get grayscale value
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
                int gray = (r + g + b) / 3;
                
                // Map to palette color
                int colorIndex = (int) ((gray / 255.0) * (palette.getColourValues(0).size() - 1));
                RgbColour color = palette.getColourValues(0).get(colorIndex);
                
                // Apply color with original alpha
                int newColor = (alpha << 24) | (color.getRgb() & 0x00ffffff);
                variant.setRGB(x, y, newColor);
            }
        }
        
        return variant;
    }
    
    private void saveVariant(BufferedImage image, String name) {
        try {
            String workingDir = System.getProperty("user.dir");
            Path outputDir = Paths.get(workingDir, "generated", "assets", "forgero", "textures", "block");
            
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            
            String baseName = name.endsWith(".png") ? name.substring(0, name.length() - 4) : name;
            Path outputPath = outputDir.resolve(baseName + ".png");
            
            Files.createDirectories(outputPath.getParent());
            ImageIO.write(image, "PNG", outputPath.toFile());
            
            // Save base texture for MoldGenerator if needed
            if (!baseName.contains("-")) {
                Path basePath = outputDir.getParent().resolve("block/" + baseName + ".png");
                Files.createDirectories(basePath.getParent());
                ImageIO.write(image, "PNG", basePath.toFile());
            }
        } catch (Exception e) {
            // Silently fail on texture save errors
        }
    }
}
