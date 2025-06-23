package com.sigmundgranaas.forgero.smithing.resource;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.texture.V2.Palette;
import com.sigmundgranaas.forgero.core.texture.V2.TextureGenerator;
import com.sigmundgranaas.forgero.core.texture.utils.RgbColour;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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
        Forgero.LOGGER.info("Reloading fluid textures...");
        try {
            generateFluidTextures();
            Forgero.LOGGER.info("Successfully reloaded fluid textures");
        } catch (Exception e) {
            Forgero.LOGGER.error("Failed to reload fluid textures: {}", e.getMessage(), e);
        }
    }
    
    public void generateFluidTextures() {
        Forgero.LOGGER.info("Starting fluid texture generation...");
        
        Map<String, Palette> palettes = textureGenerator.getPaletteMap();
        if (palettes.isEmpty()) {
            Forgero.LOGGER.warn("No palettes found for fluid texture generation");
            return;
        }
        
        Forgero.LOGGER.info("Found {} palettes for fluid texture generation", palettes.size());
        
        // Load the base fluid texture
        String baseTextureName = "fluid";
        Forgero.LOGGER.info("Loading base fluid texture: {}", baseTextureName);
        
        BufferedImage baseImage = loadBaseTexture(baseTextureName);
        if (baseImage == null) {
            Forgero.LOGGER.error("Failed to load base fluid texture: {}", baseTextureName);
            return;
        }
        
        Forgero.LOGGER.info("Successfully loaded base texture: {} ({}x{})", 
            baseTextureName, baseImage.getWidth(), baseImage.getHeight());
        
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
                // Create and save the variant using the palette
                BufferedImage variant = createVariant(baseImage, palette);
                // Save as paletteName-fluid.png
                String variantName = String.format("%s-fluid", paletteName);
                saveVariant(variant, variantName);
                variantCount++;
            } catch (Exception e) {
                Forgero.LOGGER.error("Error creating variant for palette {}: {}", paletteName, e.getMessage(), e);
            }
        }
        
        Forgero.LOGGER.info("Generated {} fluid texture variants", variantCount);
    }
    
    private BufferedImage loadBaseTexture(String name) {
        // Try to find the texture file
        java.awt.image.BufferedImage image = null;

        // Try to load from resources first
        try {
            // Try to load from the mod resources
            String resourcePath = String.format("assets/forgero/textures/block/%s.png", name);
            try (var stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (stream != null) {
                    image = ImageIO.read(stream);
                    Forgero.LOGGER.debug("Loaded base texture from resources: {}", resourcePath);
                    return image;
                }
            }

            // If not found in resources, try the Minecraft resource manager
            Identifier id = new Identifier("forgero", String.format("textures/block/%s.png", name));
            var resourceManager = MinecraftClient.getInstance().getResourceManager();
            if (resourceManager != null) {
                var resource = resourceManager.getResource(id).orElse(null);
                if (resource != null) {
                    try (var stream = resource.getInputStream()) {
                        image = ImageIO.read(stream);
                        Forgero.LOGGER.debug("Loaded base texture from resource manager: {}", id);
                        return image;
                    }
                }
            }

            // Try direct file access as a last resort
            Path filePath = Paths.get("src/main/resources/assets/forgero/textures/block", name + ".png");
            if (Files.exists(filePath)) {
                image = ImageIO.read(filePath.toFile());
                Forgero.LOGGER.debug("Loaded base texture from file: {}", filePath);
                return image;
            }

            Forgero.LOGGER.error("Failed to find base texture: {}", name);
            return null;
        } catch (Exception e) {
            Forgero.LOGGER.error("Failed to load base texture: {}", name, e);
            return null;
        }
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
            // Get the working directory
            String workingDir = System.getProperty("user.dir");
            Forgero.LOGGER.debug("Current working directory: {}", workingDir);
            
            // This will be in the generated resources directory
            Path outputDir = Paths.get(workingDir, "generated", "assets", "forgero", "textures", "block");
            
            // Create parent directories if they don't exist
            if (!Files.exists(outputDir)) {
                Forgero.LOGGER.debug("Creating directory: {}", outputDir);
                Files.createDirectories(outputDir);
            }
            
            // Ensure the name doesn't already end with .png
            String baseName = name.endsWith(".png") ? name.substring(0, name.length() - 4) : name;
            
            // Save with the palette name prefix (e.g., "iron-fluid_still.png")
            Path outputPath = outputDir.resolve(baseName + ".png");
            Forgero.LOGGER.debug("Saving fluid texture to: {}", outputPath.toAbsolutePath());
            
            // Ensure the parent directory exists
            Files.createDirectories(outputPath.getParent());
            
            // Write the image
            boolean success = ImageIO.write(image, "PNG", outputPath.toFile());
            if (success) {
                Forgero.LOGGER.info("Successfully generated fluid texture: {}", outputPath.toAbsolutePath());
                
                // Also save a copy in the resources directory for development
                Path devOutputPath = Paths.get(workingDir, "src", "main", "resources", "assets", "forgero", "textures", "block", baseName + ".png");
                if (!Files.exists(devOutputPath.getParent())) {
                    Files.createDirectories(devOutputPath.getParent());
                }
                ImageIO.write(image, "PNG", devOutputPath.toFile());
                Forgero.LOGGER.info("Saved development copy to: {}", devOutputPath.toAbsolutePath());
                
                // If this is a base texture (no palette name), also save it without any prefix
                // for MoldGenerator to find
                if (!baseName.contains("-")) {
                    Path basePath = outputDir.getParent().resolve("block/" + baseName + ".png");
                    Files.createDirectories(basePath.getParent());
                    ImageIO.write(image, "PNG", basePath.toFile());
                    Forgero.LOGGER.info("Saved base texture for MoldGenerator: {}", basePath.toAbsolutePath());
                }
            } else {
                Forgero.LOGGER.error("Failed to write image data for: {}", outputPath);
            }
        } catch (IOException e) {
            Forgero.LOGGER.error("Failed to save fluid texture variant '{}': {}", name, e.getMessage());
            Forgero.LOGGER.debug("Stack trace:", e);
        } catch (Exception e) {
            Forgero.LOGGER.error("Unexpected error while saving fluid texture variant '{}': {}", name, e.getMessage());
            Forgero.LOGGER.debug("Stack trace:", e);
        }
    }
}
