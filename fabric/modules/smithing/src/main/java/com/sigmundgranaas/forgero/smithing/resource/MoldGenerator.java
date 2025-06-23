package com.sigmundgranaas.forgero.smithing.resource;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.fabric.client.texture.FabricTextureLoader;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.DynamicResourceGenerator;
import com.sigmundgranaas.forgero.smithing.ForgeroSmithingInitializer;
import com.sigmundgranaas.forgero.smithing.block.custom.MoldBlock;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.blockstate.JVariant;
import net.devtech.arrp.json.loot.JLootTable;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Generator that creates mold blocks based on texture files
 */
public class MoldGenerator implements DynamicResourceGenerator {

    // Path within the resources directory for templates
    private static final String TEMPLATE_PATH = "templates/textures/main";
    private static final String TEXTURE_EXTENSION = ".png";

    // Get the actual source directory path from the project - corrected paths
    private static final String DEV_TEXTURE_PATH = FabricLoader.getInstance().getGameDir()
            .resolve("../content/forgero-vanilla/src/main/resources/assets/forgero/templates/textures/main").normalize().toString();

    // Alternative path for when running in the build environment
    private static final String ALT_TEXTURE_PATH = FabricLoader.getInstance().getGameDir()
            .resolve("../content/forgero-vanilla/build/resources/main/assets/forgero/templates/textures/main").normalize().toString();

    // Additional paths to try
    private static final String[] ADDITIONAL_TEXTURE_PATHS = {
            "../../../content/forgero-vanilla/src/main/resources/assets/forgero/templates/textures/main",
            "../../content/forgero-vanilla/src/main/resources/assets/forgero/templates/textures/main",
            "../content/forgero-vanilla/src/main/resources/assets/forgero/templates/textures/main",
            "./content/forgero-vanilla/src/main/resources/assets/forgero/templates/textures/main"
    };

    private final Map<String, Block> registeredMoldBlocks = new HashMap<>();
    private ResourceManager resourceManager;

    // Singleton instance
    private static MoldGenerator INSTANCE;

    private final FabricTextureLoader textureLoader = new FabricTextureLoader(id -> Optional.empty()); // Replace with actual resource getter if needed

    public MoldGenerator() {
        INSTANCE = this;

        // Register a resource reload listener to get access to the ResourceManager
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier("forgero", "mold_textures");
            }

            @Override
            public void reload(ResourceManager resourceManager) {
                MoldGenerator.this.resourceManager = resourceManager;
            }
        });
    }

    /**
     * Get the singleton instance of the MoldGenerator
     *
     * @return The MoldGenerator instance
     */
    public static MoldGenerator getInstance() {
        return INSTANCE;
    }

    /**
     * Add all registered mold blocks to the smithing item group
     */
    public void addMoldBlocksToSmithingTab() {
        if (registeredMoldBlocks.isEmpty()) {
            return;
        }

        Forgero.LOGGER.info("Adding {} dynamic mold blocks to the smithing item group", registeredMoldBlocks.size());

        ItemGroupEvents.modifyEntriesEvent(ForgeroSmithingInitializer.FORGERO_SMITHING_KEY).register(entries -> {
            for (Block block : registeredMoldBlocks.values()) {
                entries.add(block);
                Forgero.LOGGER.debug("Added mold block to smithing tab: {}", Registries.BLOCK.getId(block));
            }
        });
    }

    /**
     * Register a mold block for a specific texture
     *
     * @param textureName The texture name without extension
     * @return The registered Block
     */
    private Block registerMoldBlock(String textureName) {
        // Convert texture name to a proper block name (e.g., axe_head -> axe_head_mold)
        String moldName = textureName + "_mold";
        Identifier moldId = new Identifier("forgero", moldName);

        // Check if already registered
        if (registeredMoldBlocks.containsKey(textureName)) {
            return registeredMoldBlocks.get(textureName);
        }

        // Create a custom VoxelShape based on the texture
        VoxelShape customShape = createVoxelShapeFromTexture(textureName);
        Forgero.LOGGER.info("Created custom VoxelShape for mold: {}", moldName);

        // Create and register the block with the custom shape
        MoldBlock moldBlock = new MoldBlock(FabricBlockSettings.copyOf(Blocks.STONE_SLAB), customShape);

        // Register the block
        Registry.register(
                Registries.BLOCK,
                moldId,
                moldBlock
        );

        // Register the block item
        Registry.register(
                Registries.ITEM,
                moldId,
                new BlockItem(moldBlock, new FabricItemSettings())
        );

        // Register the block to use MoldBlockEntity
        ModBlockEntities.registerMoldBlock(moldBlock);

        // Register the block with the CUTOUT render layer for proper transparency
        BlockRenderLayerMap.INSTANCE.putBlock(moldBlock, RenderLayer.getCutout());

        Forgero.LOGGER.info("Registered mold block with custom VoxelShape and block entity support: {}", moldId);

        // Add to our tracking map
        registeredMoldBlocks.put(textureName, moldBlock);
        return moldBlock;
    }

    /**
     * Creates a VoxelShape for a mold based on the template texture.
     * Adds a 1-pixel outline to sides and bottom, removing the original template pixels
     * to create a hollow 2-pixel high mold.
     *
     * @param textureName The name of the texture to use for the shape
     * @return A hollow VoxelShape representing a mold
     */
    private VoxelShape createVoxelShapeFromTexture(String textureName) {
        // Get the texture image
        java.awt.image.BufferedImage originalImage = loadTextureImage(textureName);

        if (originalImage == null) {
            Forgero.LOGGER.warn("Couldn't load texture for VoxelShape creation. Using default shape.");
            // Return a default shape if we couldn't analyze the texture
            return Block.createCuboidShape(0, 0, 0, 16, 2, 16);
        }

        // Create a grid to track which pixels are colored
        boolean[][] coloredPixels = new boolean[16][16];

        // Scan for colored (non-transparent) pixels
        for (int x = 0; x < Math.min(originalImage.getWidth(), 16); x++) {
            for (int y = 0; y < Math.min(originalImage.getHeight(), 16); y++) {
                int pixel = originalImage.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;

                if (alpha > 10) { // Non-transparent pixel
                    coloredPixels[x][y] = true;
                }
            }
        }

        // Create a grid for the outline (colored pixels + 1 pixel outline)
        boolean[][] outlinePixels = new boolean[16][16];

        // Add colored pixels to outline
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (coloredPixels[x][y]) {
                    outlinePixels[x][y] = true;

                    // Add outline pixels (adjacent to colored pixels)
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = x + dx;
                            int ny = y + dy;

                            // Check bounds
                            if (nx >= 0 && nx < 16 && ny >= 0 && ny < 16) {
                                outlinePixels[nx][ny] = true;
                            }
                        }
                    }
                }
            }
        }

        // Start with empty shape
        VoxelShape finalShape = VoxelShapes.empty();

        // First add the bottom layer (all outline pixels at y=0)
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (outlinePixels[x][z]) {
                    VoxelShape pixelShape = Block.createCuboidShape(x, 0, z, x + 1, 1, z + 1);
                    finalShape = VoxelShapes.union(finalShape, pixelShape);
                }
            }
        }

        // Add walls (all outline pixels at y=1 that are on the edge)
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (outlinePixels[x][z] && !coloredPixels[x][z]) {
                    // This is an outline pixel but not an original colored pixel
                    // Check if it's on the edge (has a neighboring non-outline pixel)
                    boolean isEdge = false;

                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dz == 0) continue; // Skip self

                            int nx = x + dx;
                            int nz = z + dz;

                            // If neighbor is outside bounds or not an outline pixel, this is an edge
                            if (nx < 0 || nx >= 16 || nz < 0 || nz >= 16 || !outlinePixels[nx][nz]) {
                                isEdge = true;
                                break;
                            }
                        }
                        if (isEdge) break;
                    }

                    if (isEdge) {
                        VoxelShape wallShape = Block.createCuboidShape(x, 1, z, x + 1, 2, z + 1);
                        finalShape = VoxelShapes.union(finalShape, wallShape);
                    }
                }
            }
        }

        Forgero.LOGGER.info("Generated hollow mold VoxelShape for {} that follows colored pixels", textureName);
        return finalShape;
    }

    /**
     * Load the texture image from file or resource manager
     */
    private java.awt.image.BufferedImage loadTextureImage(String textureName) {
        // Try to find the texture file
        java.awt.image.BufferedImage originalImage = null;

        // Paths to try in order
        List<Path> pathsToCheck = new ArrayList<>();

        // Add the additional paths first since they were detected to work
        for (String additionalPath : ADDITIONAL_TEXTURE_PATHS) {
            Path resolvedPath = FabricLoader.getInstance().getGameDir().resolve(additionalPath).normalize();
            pathsToCheck.add(resolvedPath);
        }

        // Then add the standard paths as fallbacks
        pathsToCheck.add(Paths.get(DEV_TEXTURE_PATH));
        pathsToCheck.add(Paths.get(ALT_TEXTURE_PATH));

        // Try each path in order
        for (Path dirPath : pathsToCheck) {
            File file = dirPath.resolve(textureName + TEXTURE_EXTENSION).toFile();
            if (file.exists() && file.isFile()) {
                try {
                    originalImage = javax.imageio.ImageIO.read(file);
                    Forgero.LOGGER.info("Found template texture for VoxelShape at: {}", file.getAbsolutePath());
                    break;
                } catch (IOException e) {
                    Forgero.LOGGER.warn("Error reading texture file: {}", file.getAbsolutePath());
                }
            }
        }

        // If still not found, try loading from resource manager
        if (originalImage == null && resourceManager != null) {
            try {
                Identifier textureId = new Identifier("forgero", TEMPLATE_PATH + "/" + textureName + TEXTURE_EXTENSION);
                Resource resource = resourceManager.getResource(textureId).orElse(null);

                if (resource != null) {
                    try (java.io.InputStream is = resource.getInputStream()) {
                        originalImage = javax.imageio.ImageIO.read(is);
                        Forgero.LOGGER.info("Found template texture for VoxelShape from resources: {}", textureId);
                    }
                }
            } catch (IOException e) {
                Forgero.LOGGER.warn("Error reading texture from resource manager: {}", textureName);
            }
        }

        return originalImage;
    }

    /**
     * Generate resources for a mold block
     *
     * @param pack        The resource pack to add resources to
     * @param textureName The texture name without extension
     * @param block       The registered Block instance
     */
    private void generateMoldBlockResources(RuntimeResourcePack pack, String textureName, Block block) {
        // Convert texture name to a proper block name (e.g., axe_head -> axe_head_mold)
        String moldName = textureName + "_mold";
        String displayName = formatDisplayName(textureName) + " Mold";

        Identifier moldId = new Identifier("forgero", moldName);

        Forgero.LOGGER.info("Generating mold block resources for: {}", textureName);

        // Create loot table for the mold block
        pack.addLootTable(
                new Identifier(moldId.getNamespace(), "blocks/" + moldId.getPath()),
                JLootTable.loot("minecraft:block")
                        .pool(JLootTable.pool()
                                .rolls(1)
                                .entry(JLootTable.entry()
                                        .type("minecraft:item")
                                        .name(moldId.toString()))
                                .condition(JLootTable.predicate("minecraft:survives_explosion")))
        );

        // Define model paths
        String baseModelId = moldId.getNamespace() + ":block/" + moldId.getPath();
        String filledModelId = moldId.getNamespace() + ":block/" + moldId.getPath() + "_filled";
        String progress33ModelId = moldId.getNamespace() + ":block/" + moldId.getPath() + "_progress_33";
        String progress66ModelId = moldId.getNamespace() + ":block/" + moldId.getPath() + "_progress_66";

        // Create blockstate variants using a multipart approach that handles all progress values
        JVariant variant = JState.variant();

        // For filled=false states, always use the base model regardless of progress
        for (int i = 0; i <= 100; i++) {
            variant.put("filled=false,progress=" + i, JState.model(baseModelId));
        }

        // For filled=true states, use different models based on progress ranges
        variant.put("filled=true,progress=0", JState.model(filledModelId));

        // Progress 1-32 uses filled model
        for (int i = 1; i <= 32; i++) {
            variant.put("filled=true,progress=" + i, JState.model(filledModelId));
        }

        // Progress 33-65 uses 33% progress model
        for (int i = 33; i <= 65; i++) {
            variant.put("filled=true,progress=" + i, JState.model(progress33ModelId));
        }

        // Progress 66-99 uses 66% progress model
        for (int i = 66; i <= 99; i++) {
            variant.put("filled=true,progress=" + i, JState.model(progress66ModelId));
        }

        // Progress 100 (fully cooled) uses base model
        variant.put("filled=true,progress=100", JState.model(baseModelId));

        JState blockState = JState.state().add(variant);

        pack.addBlockState(
                blockState,
                new Identifier(moldId.getNamespace(), moldId.getPath())
        );

        // Create models with basic placeholder shapes
        generateBasicModels(pack, moldId, textureName);

        // Add to tags
        addTag(pack, new Identifier("minecraft", "mineable/pickaxe"), moldId);
        addTag(pack, new Identifier("minecraft", "needs_stone_tool"), moldId);
        addTag(pack, new Identifier("forgero", "items/molds"), moldId);
    }

    /**
     * Helper method to add a JSON model to the resource pack
     * @param pack The resource pack
     * @param modelId The model identifier
     * @param jsonContent The JSON content as a string
     */
    private void addJsonModel(RuntimeResourcePack pack, Identifier modelId, String jsonContent) {
        try {
            // Log for debugging
            Forgero.LOGGER.info("Adding model to pack: {}", modelId);

            // Use addAsset instead of addRawResource
            pack.addAsset(modelId, jsonContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            Forgero.LOGGER.info("Successfully added model to pack: {}", modelId);
        } catch (Exception e) {
            Forgero.LOGGER.error("Error adding model {} to resource pack: {}", modelId, e.getMessage(), e);
        }
    }

    /**
     * Generate basic block models for a mold
     *
     * @param pack The resource pack to add models to
     * @param moldId The mold identifier
     */
    private void generateBasicModels(RuntimeResourcePack pack, Identifier moldId, String textureName) {
        // Get the texture image
        java.awt.image.BufferedImage originalImage = loadTextureImage(textureName);

        if (originalImage == null) {
            Forgero.LOGGER.warn("Using default bounds for model generation");
            // Fall back to a simple rectangular model
            // ...existing code for fallback model generation...
            return;
        }

        // Create a grid to track which pixels are colored
        boolean[][] coloredPixels = new boolean[16][16];

        // Scan for colored (non-transparent) pixels
        for (int x = 0; x < Math.min(originalImage.getWidth(), 16); x++) {
            for (int y = 0; y < Math.min(originalImage.getHeight(), 16); y++) {
                int pixel = originalImage.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;

                if (alpha > 10) { // Non-transparent pixel
                    coloredPixels[x][y] = true;
                }
            }
        }

        // Create a grid for the outline (colored pixels + 1 pixel outline)
        boolean[][] outlinePixels = new boolean[16][16];

        // Add colored pixels to outline
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (coloredPixels[x][y]) {
                    outlinePixels[x][y] = true;

                    // Add outline pixels (adjacent to colored pixels)
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = x + dx;
                            int ny = y + dy;

                            // Check bounds
                            if (nx >= 0 && nx < 16 && ny >= 0 && ny < 16) {
                                outlinePixels[nx][ny] = true;
                            }
                        }
                    }
                }
            }
        }

        Forgero.LOGGER.info("Creating hollow mold model that follows colored pixels");

        // Generate the model elements
        StringBuilder elementsJson = new StringBuilder();

        // Add bottom slab for all outline pixels
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (outlinePixels[x][z]) {
                    if (elementsJson.length() > 0) {
                        elementsJson.append(",\n");
                    }

                    // For bottom elements, use terracotta for all faces
                    // Check if we need to show faces (adjacent to air/non-outline)
                    boolean showNorth = z == 0 || !outlinePixels[x][z-1];
                    boolean showSouth = z == 15 || !outlinePixels[x][z+1];
                    boolean showEast = x == 15 || !outlinePixels[x+1][z];
                    boolean showWest = x == 0 || !outlinePixels[x-1][z];

                    elementsJson.append(String.format(
                        "    {\n" +
                        "      \"from\": [%d, 0.0, %d],\n" +
                        "      \"to\": [%d, 1.0, %d],\n" +
                        "      \"faces\": {\n" +
                        "%s" +
                        "%s" +
                        "%s" +
                        "%s" +
                        "        \"up\": {\"texture\": \"#terracotta\"},\n" +
                        "        \"down\": {\"texture\": \"#terracotta\", \"cullface\": \"down\"}\n" +
                        "      }\n" +
                        "    }",
                        x, z, x + 1, z + 1,
                        showNorth ? "        \"north\": {\"texture\": \"#terracotta\"" + (z == 0 ? ", \"cullface\": \"north\"" : "") + "},\n" : "",
                        showEast ? "        \"east\": {\"texture\": \"#terracotta\"" + (x == 15 ? ", \"cullface\": \"east\"" : "") + "},\n" : "",
                        showSouth ? "        \"south\": {\"texture\": \"#terracotta\"" + (z == 15 ? ", \"cullface\": \"south\"" : "") + "},\n" : "",
                        showWest ? "        \"west\": {\"texture\": \"#terracotta\"" + (x == 0 ? ", \"cullface\": \"west\"" : "") + "},\n" : ""
                    ));
                }
            }
        }

        // Add walls for outline pixels that are not colored pixels
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (outlinePixels[x][z] && !coloredPixels[x][z]) {
                    // Check if it's on the edge (has a neighboring non-outline pixel)
                    boolean isEdge = false;

                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dz == 0) continue; // Skip self

                            int nx = x + dx;
                            int nz = z + dz;

                            // If neighbor is outside bounds or not an outline pixel, this is an edge
                            if (nx < 0 || nx >= 16 || nz < 0 || nz >= 16 || !outlinePixels[nx][nz]) {
                                isEdge = true;
                                break;
                            }
                        }
                        if (isEdge) break;
                    }

                    if (isEdge) {
                        if (elementsJson.length() > 0) {
                            elementsJson.append(",\n");
                        }

                        // Always show all side faces for wall elements regardless of neighbors
                        // This ensures the terracotta texture appears on all sides of the walls
                        elementsJson.append(String.format(
                            "    {\n" +
                            "      \"from\": [%d, 1.0, %d],\n" +
                            "      \"to\": [%d, 2.0, %d],\n" +
                            "      \"shade\": true,\n" +
                            "      \"faces\": {\n" +
                            "        \"north\": {\"texture\": \"#terracotta\"%s},\n" +
                            "        \"east\": {\"texture\": \"#terracotta\"%s},\n" +
                            "        \"south\": {\"texture\": \"#terracotta\"%s},\n" +
                            "        \"west\": {\"texture\": \"#terracotta\"%s},\n" +
                            "        \"up\": {\"texture\": \"#terracotta\"}\n" +
                            "      }\n" +
                            "    }",
                            x, z, x + 1, z + 1,
                            (z == 0 ? ", \"cullface\": \"north\"" : ""),
                            (x == 15 ? ", \"cullface\": \"east\"" : ""),
                            (z == 15 ? ", \"cullface\": \"south\"" : ""),
                            (x == 0 ? ", \"cullface\": \"west\"" : "")
                        ));
                    }
                }
            }
        }

        // Base model (empty mold) with ambient occlusion enabled - ONLY using terracotta texture for all faces
        String baseModelJson = String.format(
            "{\n" +
            "  \"parent\": \"minecraft:block/block\",\n" +
            "  \"ambientocclusion\": true,\n" +
            "  \"textures\": {\n" +
            "    \"terracotta\": \"forgero:block/terracotta\",\n" +
            "    \"particle\": \"forgero:block/terracotta\"\n" +
            "  },\n" +
            "  \"elements\": [\n%s\n  ]\n" +
            "}", elementsJson.toString());

        // Add models with proper paths

        // Base model (empty) - ONLY terracotta texture
        Identifier baseModelId = new Identifier(moldId.getNamespace(), "models/block/" + moldId.getPath() + ".json");
        pack.addAsset(baseModelId, baseModelJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Forgero.LOGGER.info("Added base model: {}", baseModelId);

        // Filled/progress models: add template voxelshape as a new element with template texture on top
        String templateElementJson = generateTemplateElementJson(textureName, coloredPixels);

        // Join elements for filled/progress models without leading comma
        String allFilledElements = elementsJson.toString();
        String allTemplateElements = templateElementJson;
        String joinedElements;
        if (!allFilledElements.isEmpty() && !allTemplateElements.isEmpty()) {
            joinedElements = allFilledElements + ",\n" + allTemplateElements;
        } else if (!allFilledElements.isEmpty()) {
            joinedElements = allFilledElements;
        } else {
            joinedElements = allTemplateElements;
        }

        // Generate dynamic fluid texture for this mold/material
        String fluidTextureName = "fluid_flow_" + textureName;
        Identifier fluidTextureId = new Identifier("forgero", "textures/block/" + fluidTextureName + ".png");
        BufferedImage fluidImage = generateDynamicFluidTexture(textureName);
        if (fluidImage != null) {
            try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                javax.imageio.ImageIO.write(fluidImage, "PNG", baos);
                pack.addAsset(fluidTextureId, baos.toByteArray());
                Forgero.LOGGER.info("Added dynamic fluid texture: {}", fluidTextureId);
            } catch (IOException e) {
                Forgero.LOGGER.error("Failed to write dynamic fluid texture for {}: {}", textureName, e.getMessage());
            }
        }

        // Use the dynamic fluid texture for the template face
        String templateTexturePath = "forgero:block/" + fluidTextureName;

        String filledModelJson = String.format(
            "{\n" +
            "  \"parent\": \"minecraft:block/block\",\n" +
            "  \"ambientocclusion\": true,\n" +
            "  \"textures\": {\n" +
            "    \"terracotta\": \"forgero:block/terracotta\",\n" +
            "    \"top\": \"forgero:block/terracotta\",\n" +
            "    \"template\": \"%s\",\n" +
            "    \"particle\": \"forgero:block/terracotta\"\n" +
            "  },\n" +
            "  \"elements\": [\n%s\n  ]\n" +
            "}", templateTexturePath, joinedElements);

        Identifier filledModelId = new Identifier(moldId.getNamespace(), "models/block/" + moldId.getPath() + "_filled.json");
        pack.addAsset(filledModelId, filledModelJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Forgero.LOGGER.info("Added filled model: {}", filledModelId);

        // Progress 33 model
        String progress33ModelJson = String.format(
            "{\n" +
            "  \"parent\": \"minecraft:block/block\",\n" +
            "  \"ambientocclusion\": true,\n" +
            "  \"textures\": {\n" +
            "    \"terracotta\": \"forgero:block/terracotta\",\n" +
            "    \"top\": \"forgero:block/terracotta\",\n" +
            "    \"template\": \"%s\",\n" +
            "    \"particle\": \"forgero:block/terracotta\"\n" +
            "  },\n" +
            "  \"elements\": [\n%s\n  ]\n" +
            "}", templateTexturePath, joinedElements);

        Identifier progress33ModelId = new Identifier(moldId.getNamespace(), "models/block/" + moldId.getPath() + "_progress_33.json");
        pack.addAsset(progress33ModelId, progress33ModelJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Forgero.LOGGER.info("Added progress 33 model: {}", progress33ModelId);

        // Progress 66 model
        String progress66ModelJson = String.format(
            "{\n" +
            "  \"parent\": \"minecraft:block/block\",\n" +
            "  \"ambientocclusion\": true,\n" +
            "  \"textures\": {\n" +
            "    \"terracotta\": \"forgero:block/terracotta\",\n" +
            "    \"top\": \"forgero:block/terracotta\",\n" +
            "    \"template\": \"%s\",\n" +
            "    \"particle\": \"forgero:block/terracotta\"\n" +
            "  },\n" +
            "  \"elements\": [\n%s\n  ]\n" +
            "}", templateTexturePath, joinedElements);

        Identifier progress66ModelId = new Identifier(moldId.getNamespace(), "models/block/" + moldId.getPath() + "_progress_66.json");
        pack.addAsset(progress66ModelId, progress66ModelJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Forgero.LOGGER.info("Added progress 66 model: {}", progress66ModelId);

        // Item model
        String itemModelJson = String.format(
            "{\n" +
            "  \"parent\": \"%s:block/%s\"\n" +
            "}",
            moldId.getNamespace(), moldId.getPath()
        );

        Identifier itemModelId = new Identifier(moldId.getNamespace(), "models/item/" + moldId.getPath() + ".json");
        pack.addAsset(itemModelId, itemModelJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Forgero.LOGGER.info("Added item model: {}", itemModelId);

        // Verify all models were added
        Forgero.LOGGER.info("Successfully added all model files for hollow mold: {}", moldId);
    }

    /**
     * Generate a JSON element for the template voxelshape (filled state).
     * This adds a 1x1x1 cube for each colored pixel at y=1..2, with template texture on up face.
     */
    private String generateTemplateElementJson(String textureName, boolean[][] coloredPixels) {
        StringBuilder templateElements = new StringBuilder();
        boolean first = true;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (coloredPixels[x][z]) {
                    if (!first) {
                        templateElements.append(",\n");
                    }
                    templateElements.append(String.format(
                        "    {\n" +
                        "      \"from\": [%d, 1.0, %d],\n" +
                        "      \"to\": [%d, 2.0, %d],\n" +
                        "      \"faces\": {\n" +
                        "        \"up\": {\"texture\": \"#template\"},\n" +
                        "        \"down\": {\"texture\": \"#terracotta\"},\n" +
                        "        \"north\": {\"texture\": \"#terracotta\"},\n" +
                        "        \"south\": {\"texture\": \"#terracotta\"},\n" +
                        "        \"east\": {\"texture\": \"#terracotta\"},\n" +
                        "        \"west\": {\"texture\": \"#terracotta\"}\n" +
                        "      }\n" +
                        "    }",
                        x, z, x + 1, z + 1
                    ));
                    first = false;
                }
            }
        }
        return templateElements.toString();
    }

    /**
     * Format a texture name for display by converting snake_case to Title Case
     * @param textureName The texture name to format
     * @return A formatted display name
     */
    private String formatDisplayName(String textureName) {
        if (textureName == null || textureName.isEmpty()) {
            return "";
        }

        // Split by underscores or hyphens
        String[] words = textureName.split("[_-]");
        StringBuilder displayName = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                // Capitalize first letter of each word
                displayName.append(Character.toUpperCase(word.charAt(0)));

                // Add the rest of the word
                if (word.length() > 1) {
                    displayName.append(word.substring(1).toLowerCase());
                }

                displayName.append(" ");
            }
        }

        // Remove trailing space and return
        return displayName.toString().trim();
    }

    /**
     * Add an entry to a tag in the resource pack
     * @param pack The resource pack
     * @param tagId The tag identifier (e.g., "minecraft:mineable/pickaxe")
     * @param entryId The entry to add to the tag
     */
    private void addTag(RuntimeResourcePack pack, Identifier tagId, Identifier entryId) {
        // Get or create the tag
        net.devtech.arrp.json.tags.JTag tag = net.devtech.arrp.json.tags.JTag.tag();
        tag.add(entryId);

        // Add the tag to the resource pack
        pack.addTag(tagId, tag);
    }

    /**
     * Find all texture files in the template directory
     *
     * @return List of texture names without extension
     */
    private List<String> findTextureFiles() {
        List<String> textureFiles = new ArrayList<>();

        // First check the additional paths that we've confirmed work
        List<Path> pathsToCheck = new ArrayList<>();

        // Add the additional paths first since they were detected to work
        for (String additionalPath : ADDITIONAL_TEXTURE_PATHS) {
            Path resolvedPath = FabricLoader.getInstance().getGameDir().resolve(additionalPath).normalize();
            pathsToCheck.add(resolvedPath);
        }

        // Then add the standard paths as fallbacks
        pathsToCheck.add(Paths.get(DEV_TEXTURE_PATH));
        pathsToCheck.add(Paths.get(ALT_TEXTURE_PATH));

        // Try each path in order
        for (Path dirPath : pathsToCheck) {
            if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
                try {
                    try (Stream<Path> stream = Files.list(dirPath)) {
                        List<String> foundTextures = stream
                                .filter(Files::isRegularFile)
                                .map(p -> p.getFileName().toString())
                                .filter(name -> name.endsWith(TEXTURE_EXTENSION))
                                .map(name -> name.substring(0, name.length() - TEXTURE_EXTENSION.length()))
                                .collect(Collectors.toList());

                        if (!foundTextures.isEmpty()) {
                            Forgero.LOGGER.info("Found {} textures in {} for mold generation", foundTextures.size(), dirPath);
                            for (String texture : foundTextures) {
                                Forgero.LOGGER.info("Found texture for mold: {}", texture);
                            }
                            textureFiles.addAll(foundTextures);
                            break; // Stop searching if we found textures
                        }
                    }
                } catch (Exception e) {
                    Forgero.LOGGER.warn("Error scanning texture directory: {}", dirPath);
                }
            }
        }

        // If still no textures found, try using the resource manager
        if (textureFiles.isEmpty() && resourceManager != null) {
            try {
                Map<Identifier, Resource> resources = resourceManager.findResources(TEMPLATE_PATH,
                        id -> id.getPath().endsWith(TEXTURE_EXTENSION));

                if (!resources.isEmpty()) {
                    Forgero.LOGGER.info("Found {} texture resources", resources.size());
                    for (Identifier id : resources.keySet()) {
                        String path = id.getPath();
                        if (path.endsWith(TEXTURE_EXTENSION)) {
                            String fileName = path.substring(path.lastIndexOf('/') + 1);
                            String textureName = fileName.substring(0, fileName.length() - TEXTURE_EXTENSION.length());
                            textureFiles.add(textureName);
                            Forgero.LOGGER.info("Found resource texture: {}", textureName);
                        }
                    }
                }
            } catch (Exception e) {
                Forgero.LOGGER.error("Error finding resource textures: {}", e.getMessage());
            }
        }

        return textureFiles;
    }

    /**
     * Implementation of the DynamicResourceGenerator interface method
     * This is the main entry point for generating all mold-related resources
     */
    @Override
    public void generate(RuntimeResourcePack pack) {
        Forgero.LOGGER.info("Generating mold blocks from textures");

        List<String> textureNames = findTextureFiles();

        if (textureNames.isEmpty()) {
            Forgero.LOGGER.warn("No texture files found for mold generation");
            return;
        }

        Forgero.LOGGER.info("Found {} texture files for mold generation", textureNames.size());

        // Process all textures to create molds
        for (String textureName : textureNames) {
            try {
                // Register the mold block for this texture
                Block moldBlock = registerMoldBlock(textureName);

                // Convert texture name to mold ID for consistent usage
                String moldName = textureName + "_mold";
                Identifier moldId = new Identifier("forgero", moldName);

                // Generate all necessary resources for the mold block
                generateMoldBlockResources(pack, textureName, moldBlock);
            } catch (Exception e) {
                Forgero.LOGGER.error("Failed to process texture for mold: " + textureName, e);
            }
        }

        Forgero.LOGGER.info("Successfully generated resources for {} mold blocks", registeredMoldBlocks.size());

        // Add the registered mold blocks to the smithing creative tab
        addMoldBlocksToSmithingTab();

        // After all molds are registered, rebuild the BlockEntityType to include all molds
        com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities.rebuildMoldBlockEntityType();
    }

    /**
     * Analyze a texture to determine the bounds of the colored pixels
     *
     * @param textureName The texture name to analyze
     * @return int array with [minX, minY, maxX, maxY] or null if texture not found
     */
    private int[] analyzeTextureBounds(String textureName) {
        java.awt.image.BufferedImage originalImage = loadTextureImage(textureName);

        if (originalImage == null) {
            Forgero.LOGGER.error("Could not find texture for VoxelShape generation: {}", textureName);
            return null;
        }

        // Initialize bounds to extreme values
        int minX = originalImage.getWidth();
        int minY = originalImage.getHeight();
        int maxX = 0;
        int maxY = 0;

        // Scan for colored (non-transparent) pixels
        for (int x = 0; x < originalImage.getWidth(); x++) {
            for (int y = 0; y < originalImage.getHeight(); y++) {
                int pixel = originalImage.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;

                if (alpha > 10) { // Non-transparent pixel
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        // If no colored pixels found, use default full texture bounds
        if (minX > maxX || minY > maxY) {
            return new int[]{0, 0, originalImage.getWidth() - 1, originalImage.getHeight() - 1};
        }

        return new int[]{minX, minY, maxX, maxY};
    }

    /**
     * Creates a VoxelShape for a mold based on the template texture.
     * Adds a 1-pixel outline to sides and bottom.
     *
     * @param textureName The name of the texture to use for the shape
     * @return A VoxelShape based on colored pixels with added outlines
     */
    private String createVoxelShapeCode(String textureName) {
        // Get bounds of the colored pixels in the template
        int[] bounds = analyzeTextureBounds(textureName);

        if (bounds == null) {
            Forgero.LOGGER.warn("Couldn't analyze texture bounds for VoxelShape creation. Using default shape.");
            // Return a default shape if we couldn't analyze the texture
            return "Block.createCuboidShape(0, 0, 0, 16, 2, 16)";
        }

        // Extract bounds and add 1-pixel outline to sides and bottom
        int minX = Math.max(0, bounds[0] - 1); // Subtract 1 for outline, but don't go below 0
        int minZ = Math.max(0, bounds[1] - 1); // Subtract 1 for outline, but don't go below 0
        int maxX = Math.min(16, bounds[2] + 1); // Add 1 for outline, but don't exceed 16
        int maxZ = Math.min(16, bounds[3] + 1); // Add 1 for outline, but don't exceed 16

        // Convert pixel coordinates (0-15) to block coordinates (0-16)
        float blockMinX = minX;
        float blockMinY = 0.0f; // Always start at bottom of block
        float blockMinZ = minZ;
        float blockMaxX = maxX;
        float blockMaxY = 2.0f; // Fixed height at 2 pixels (1/8 of a block)
        float blockMaxZ = maxZ;

        Forgero.LOGGER.info("Generated VoxelShape for {} with bounds: [{}, {}, {}, {}, {}, {}]",
                textureName, blockMinX, blockMinY, blockMinZ, blockMaxX, blockMaxY, blockMaxZ);

        // Create a VoxelShape definition
        return String.format("Block.createCuboidShape(%.1f, %.1f, %.1f, %.1f, %.1f, %.1f)",
                blockMinX, blockMinY, blockMinZ, blockMaxX, blockMaxY, blockMaxZ);
    }

    /**
     * Generate JSON elements for the filled/progress mold models.
     * Uses terracotta for all faces except the top of the wall elements, which uses the "top" texture.
     */
    private String generateFilledElementsJson(boolean[][] coloredPixels, boolean[][] outlinePixels) {
        StringBuilder elementsJson = new StringBuilder();

        // Add bottom slab for all outline pixels
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (outlinePixels[x][z]) {
                    if (elementsJson.length() > 0) {
                        elementsJson.append(",\n");
                    }
                    boolean showNorth = z == 0 || !outlinePixels[x][z-1];
                    boolean showSouth = z == 15 || !outlinePixels[x][z+1];
                    boolean showEast = x == 15 || !outlinePixels[x+1][z];
                    boolean showWest = x == 0 || !outlinePixels[x-1][z];

                    elementsJson.append(String.format(
                        "    {\n" +
                        "      \"from\": [%d, 0.0, %d],\n" +
                        "      \"to\": [%d, 1.0, %d],\n" +
                        "      \"faces\": {\n" +
                        "%s" +
                        "%s" +
                        "%s" +
                        "%s" +
                        "        \"up\": {\"texture\": \"#terracotta\"},\n" +
                        "        \"down\": {\"texture\": \"#terracotta\", \"cullface\": \"down\"}\n" +
                        "      }\n" +
                        "    }",
                        x, z, x + 1, z + 1,
                        showNorth ? "        \"north\": {\"texture\": \"#terracotta\"" + (z == 0 ? ", \"cullface\": \"north\"" : "") + "},\n" : "",
                        showEast ? "        \"east\": {\"texture\": \"#terracotta\"" + (x == 15 ? ", \"cullface\": \"east\"" : "") + "},\n" : "",
                        showSouth ? "        \"south\": {\"texture\": \"#terracotta\"" + (z == 15 ? ", \"cullface\": \"south\"" : "") + "},\n" : "",
                        showWest ? "        \"west\": {\"texture\": \"#terracotta\"" + (x == 0 ? ", \"cullface\": \"west\"" : "") + "},\n" : ""
                    ));
                }
            }
        }

        // Add walls for outline pixels that are not colored pixels
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (outlinePixels[x][z] && !coloredPixels[x][z]) {
                    boolean isEdge = false;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dz == 0) continue;
                            int nx = x + dx;
                            int nz = z + dz;
                            if (nx < 0 || nx >= 16 || nz < 0 || nz >= 16 || !outlinePixels[nx][nz]) {
                                isEdge = true;
                                break;
                            }
                        }
                        if (isEdge) break;
                    }
                    if (isEdge) {
                        if (elementsJson.length() > 0) {
                            elementsJson.append(",\n");
                        }
                        elementsJson.append(String.format(
                            "    {\n" +
                            "      \"from\": [%d, 1.0, %d],\n" +
                            "      \"to\": [%d, 2.0, %d],\n" +
                            "      \"shade\": true,\n" +
                            "      \"faces\": {\n" +
                            "        \"north\": {\"texture\": \"#terracotta\"%s},\n" +
                            "        \"east\": {\"texture\": \"#terracotta\"%s},\n" +
                            "        \"south\": {\"texture\": \"#terracotta\"%s},\n" +
                            "        \"west\": {\"texture\": \"#terracotta\"%s},\n" +
                            "        \"up\": {\"texture\": \"#top\"}\n" +
                            "      }\n" +
                            "    }",
                            x, z, x + 1, z + 1,
                            (z == 0 ? ", \"cullface\": \"north\"" : ""),
                            (x == 15 ? ", \"cullface\": \"east\"" : ""),
                            (z == 15 ? ", \"cullface\": \"south\"" : ""),
                            (x == 0 ? ", \"cullface\": \"west\"" : "")
                        ));
                    }
                }
            }
        }

        return elementsJson.toString();
    }

    /**
     * Generate a dynamic fluid texture based on the material/template.
     * This version uses the palette system from FabricTextureLoader to colorize the fluid_flow texture.
     */
    private BufferedImage generateDynamicFluidTexture(String textureName) {
        try {
            // Use the path string for PaletteTemplateIdentifier constructor
            String baseFluidPath = "block/fluid_flow";
            com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteTemplateIdentifier baseFluidIdentifier =
                new com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteTemplateIdentifier(baseFluidPath);

            BufferedImage baseFluid = textureLoader.getResource(baseFluidIdentifier).getImage();

            // Use the palette system to get the palette for this template/material
            com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier paletteId =
                new com.sigmundgranaas.forgero.core.identifier.texture.toolpart.PaletteIdentifier(textureName);

            BufferedImage palette = textureLoader.getResource(paletteId).getImage();

            if (baseFluid == null || palette == null) {
                return null;
            }

            int paletteSize = Math.max(palette.getWidth(), palette.getHeight());
            int[] paletteColors = new int[paletteSize];
            for (int i = 0; i < paletteSize; i++) {
                paletteColors[i] = palette.getRGB(
                    palette.getWidth() == 1 ? 0 : i,
                    palette.getHeight() == 1 ? 0 : i
                );
            }

            BufferedImage result = new BufferedImage(baseFluid.getWidth(), baseFluid.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < baseFluid.getWidth(); x++) {
                for (int y = 0; y < baseFluid.getHeight(); y++) {
                    int pixel = baseFluid.getRGB(x, y);
                    int alpha = (pixel >> 24) & 0xff;
                    int gray = (pixel >> 16) & 0xff; // Assume gray, so R=G=B

                    int paletteIndex = (int) ((gray / 255.0) * (paletteSize - 1));
                    int color = paletteColors[paletteIndex];

                    int colored = (alpha << 24) | (color & 0x00ffffff);
                    result.setRGB(x, y, colored);
                }
            }
            return result;
        } catch (Exception e) {
            Forgero.LOGGER.error("Failed to generate palette-based fluid texture for {}: {}", textureName, e.getMessage());
            return null;
        }
    }
}
