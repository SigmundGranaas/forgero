package com.sigmundgranaas.forgero.smithing.resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

/**
 * Generator that creates mold blocks based on texture files
 */
public class MoldGenerator implements DynamicResourceGenerator {

    // Path within the resources directory for templates
    private static final String TEMPLATE_PATH = "assets/forgero/templates/textures/molds";
    private static final String TEXTURE_EXTENSION = ".png";

    // Only use this directory for textures (relative to project root)
    private static final String MOLD_TEXTURE_PATH = "fabric/modules/smithing/src/main/resources/assets/forgero/templates/textures/molds";

    private final Map<String, Block> registeredMoldBlocks = new HashMap<>();
    private ResourceManager resourceManager;

    // Map to track which textures have successfully been mapped to VoxelShapes
    private final Map<String, Boolean> textureToShapeMap = new HashMap<>();

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
     * Adds a 1-pixel outline to sides and bottom.
     *
     * @param textureName The name of the texture to use for the shape
     * @return A VoxelShape based on colored pixels with added outlines
     */
    private VoxelShape createVoxelShapeFromTexture(String textureName) {
        // Get the texture image
        java.awt.image.BufferedImage originalImage = loadTextureImage(textureName);

        if (originalImage == null) {
            Forgero.LOGGER.warn("Couldn't load texture for VoxelShape creation. Using default shape for: " + textureName);
            // Return a default shape if we couldn't analyze the texture
            return Block.createCuboidShape(0, 0, 0, 16, 2, 16);
        }

        // Create a grid to track which pixels are colored
        boolean[][] coloredPixels = new boolean[16][16];

        // Ensure we're working with a consistent size
        int width = Math.min(originalImage.getWidth(), 16);
        int height = Math.min(originalImage.getHeight(), 16);

        // --- Remove border padding: Only consider inner area, ignore border pixels ---
        int alphaThreshold = 30;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Ignore border pixels (padding)
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) continue;
                int pixel = originalImage.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;
                if (alpha >= alphaThreshold) {
                    coloredPixels[x][y] = true;
                }
            }
        }

        // Create a grid for the outline (colored pixels + 1 pixel outline)
        boolean[][] outlinePixels = new boolean[16][16];

        // First, mark all colored pixels in the outline
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (coloredPixels[x][y]) {
                    outlinePixels[x][y] = true;
                }
            }
        }

        // Generate the outline with fixed width
        int outlineSize = 1;
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (coloredPixels[x][y]) {
                    outlinePixels[x][y] = true;
                    for (int dx = -outlineSize; dx <= outlineSize; dx++) {
                        for (int dy = -outlineSize; dy <= outlineSize; dy++) {
                            if (dx == 0 && dy == 0) continue;
                            int distance = Math.abs(dx) + Math.abs(dy);
                            if (distance <= outlineSize) {
                                int nx = x + dx;
                                int ny = y + dy;
                                if (nx >= 0 && nx < 16 && ny >= 0 && ny < 16) {
                                    outlinePixels[nx][ny] = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Centering logic start ---
        // Find bounding box of outlinePixels
        int minX = 16, maxX = -1, minZ = 16, maxZ = -1;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (outlinePixels[x][z]) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (z < minZ) minZ = z;
                    if (z > maxZ) maxZ = z;
                }
            }
        }
        // Only center if there are any outline pixels
        int dx = 0, dz = 0;
        if (maxX >= minX && maxZ >= minZ) {
            int shapeWidth = maxX - minX + 1;
            int shapeDepth = maxZ - minZ + 1;
            int centerX = minX + shapeWidth / 2;
            int centerZ = minZ + shapeDepth / 2;
            int gridCenter = 8; // Center of 16x16 grid
            dx = gridCenter - centerX;
            dz = gridCenter - centerZ;
        }
        // Create shifted versions of outlinePixels and coloredPixels
        boolean[][] shiftedOutline = new boolean[16][16];
        boolean[][] shiftedColored = new boolean[16][16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int sx = x + dx;
                int sz = z + dz;
                if (sx >= 0 && sx < 16 && sz >= 0 && sz < 16) {
                    shiftedOutline[sx][sz] = outlinePixels[x][z];
                    shiftedColored[sx][sz] = coloredPixels[x][z];
                }
            }
        }
        // Use shiftedOutline and shiftedColored from here on
        outlinePixels = shiftedOutline;
        coloredPixels = shiftedColored;
        // --- Centering logic end ---

        // Start with empty shape
        VoxelShape finalShape = VoxelShapes.empty();

        // Add the bottom layer (all outline pixels at y=0)
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (outlinePixels[x][z]) {
                    VoxelShape pixelShape = Block.createCuboidShape(x, 0, z, x + 1, 1, z + 1);
                    finalShape = VoxelShapes.union(finalShape, pixelShape);
                }
            }
        }

        // Standard wall handling logic
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                boolean shouldAddWall = false;
                if (outlinePixels[x][z] && (x == 0 || x == 15 || z == 0 || z == 15)) {
                    shouldAddWall = true;
                }
                else if (outlinePixels[x][z] && !coloredPixels[x][z]) {
                    boolean isAtTemplateEdge = (x == 0 || x == 15 || z == 0 || z == 15);
                    if (isAtTemplateEdge) {
                        shouldAddWall = true;
                    } else {
                        int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
                        for (int[] dir : directions) {
                            int nx = x + dir[0];
                            int nz = z + dir[1];
                            if (nx < 0 || nx >= 16 || nz < 0 || nz >= 16 || !outlinePixels[nx][nz] || coloredPixels[nx][nz]) {
                                shouldAddWall = true;
                                break;
                            }
                        }
                    }
                }
                if (shouldAddWall) {
                    VoxelShape wallShape = Block.createCuboidShape(x, 1, z, x + 1, 2, z + 1);
                    finalShape = VoxelShapes.union(finalShape, wallShape);
                }
            }
        }

        // Additional pass for edge pixel wall reinforcement
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (outlinePixels[x][z] && !coloredPixels[x][z]) {
                    boolean needsEdgeWall = false;
                    if (x == 0 || x == 15 || z == 0 || z == 15) {
                        needsEdgeWall = true;
                    }
                    int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
                    for (int[] dir : directions) {
                        int nx = x + dir[0];
                        int nz = z + dir[1];
                        if (nx >= 0 && nx < 16 && nz >= 0 && nz < 16 && coloredPixels[nx][nz]) {
                            if (nx == 0 || nx == 15 || nz == 0 || nz == 15) {
                                needsEdgeWall = true;
                                break;
                            }
                        }
                    }
                    if (needsEdgeWall) {
                        VoxelShape wallShape = Block.createCuboidShape(x, 1, z, x + 1, 2, z + 1);
                        finalShape = VoxelShapes.union(finalShape, wallShape);
                    }
                }
            }
        }

        // Track successful mapping
        textureToShapeMap.put(textureName, true);

        return finalShape;
    }

    /**
     * Load the texture image from the classpath resource or resource manager
     */
    private java.awt.image.BufferedImage loadTextureImage(String textureName) {
        java.awt.image.BufferedImage originalImage = null;

        // Try loading from classpath (resources)
        String resourcePath = TEMPLATE_PATH + "/" + textureName + TEXTURE_EXTENSION;
        try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is != null) {
                originalImage = javax.imageio.ImageIO.read(is);
                Forgero.LOGGER.info("Found template texture for VoxelShape at: {}", resourcePath);
            }
        } catch (IOException e) {
            Forgero.LOGGER.warn("Error reading texture file: {}", resourcePath);
        }

        // If still not found, try loading from resource manager
        if (originalImage == null && resourceManager != null) {
            try {
                Identifier textureId = new Identifier("forgero", "templates/textures/molds/" + textureName + TEXTURE_EXTENSION);
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

        // Define model path
        String baseModelId = moldId.getNamespace() + ":block/" + moldId.getPath();

        // --- Blockstate: All states reference only the base model ---
        JVariant variant = JState.variant();
        for (int i = 0; i <= 100; i++) {
            variant.put("filled=false,progress=" + i, JState.model(baseModelId));
            variant.put("filled=true,progress=" + i, JState.model(baseModelId));
        }
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
            Forgero.LOGGER.warn("Using default bounds for model generation for texture: " + textureName);
            // Fall back to a simple rectangular model
            // ...existing code for fallback model generation...
            return;
        }

        // Create a grid to track which pixels are colored
        boolean[][] coloredPixels = new boolean[16][16];

        // Ensure we're working with a consistent size
        int width = Math.min(originalImage.getWidth(), 16);
        int height = Math.min(originalImage.getHeight(), 16);

        int alphaThreshold = 1;

        // --- Remove border padding: Only consider inner area, ignore border pixels ---
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Ignore border pixels (padding)
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) continue;
                int pixel = originalImage.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;
                // Use same threshold as in createVoxelShapeFromTexture for consistency
                if (alpha >= alphaThreshold) {
                    coloredPixels[x][y] = true;
                }
            }
        }

        // Create a grid for the outline (colored pixels + 1 pixel outline)
        boolean[][] outlinePixels = new boolean[16][16];

        // First, mark all colored pixels in the outline
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (coloredPixels[x][y]) {
                    outlinePixels[x][y] = true;
                }
            }
        }

        // Generate the outline with fixed width
        int outlineSize = 1;
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                if (coloredPixels[x][y]) {
                    outlinePixels[x][y] = true;
                    for (int dx = -outlineSize; dx <= outlineSize; dx++) {
                        for (int dy = -outlineSize; dy <= outlineSize; dy++) {
                            if (dx == 0 && dy == 0) continue;
                            int distance = Math.abs(dx) + Math.abs(dy);
                            if (distance <= outlineSize) {
                                int nx = x + dx;
                                int ny = y + dy;
                                if (nx >= 0 && nx < 16 && ny >= 0 && ny < 16) {
                                    outlinePixels[nx][ny] = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Centering logic start (models) ---
        // Find bounding box of outlinePixels
        int minX = 16, maxX = -1, minZ = 16, maxZ = -1;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (outlinePixels[x][z]) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (z < minZ) minZ = z;
                    if (z > maxZ) maxZ = z;
                }
            }
        }
        int dx = 0, dz = 0;
        if (maxX >= minX && maxZ >= minZ) {
            int shapeWidth = maxX - minX + 1;
            int shapeDepth = maxZ - minZ + 1;
            int centerX = minX + shapeWidth / 2;
            int centerZ = minZ + shapeDepth / 2;
            int gridCenter = 8;
            dx = gridCenter - centerX;
            dz = gridCenter - centerZ;
        }
        // Create shifted versions of outlinePixels and coloredPixels
        boolean[][] shiftedOutline = new boolean[16][16];
        boolean[][] shiftedColored = new boolean[16][16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int sx = x + dx;
                int sz = z + dz;
                if (sx >= 0 && sx < 16 && sz >= 0 && sz < 16) {
                    shiftedOutline[sx][sz] = outlinePixels[x][z];
                    shiftedColored[sx][sz] = coloredPixels[x][z];
                }
            }
        }
        outlinePixels = shiftedOutline;
        coloredPixels = shiftedColored;
        // --- Centering logic end (models) ---

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

                    for (int dx2 = -1; dx2 <= 1; dx2++) {
                        for (int dz2 = -1; dz2 <= 1; dz2++) {
                            if (dx2 == 0 && dz2 == 0) continue; // Skip self

                            int nx = x + dx2;
                            int nz = z + dz2;

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

        // Base model (empty)
        Identifier baseModelId = new Identifier(moldId.getNamespace(), "models/block/" + moldId.getPath() + ".json");
        pack.addAsset(baseModelId, baseModelJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Forgero.LOGGER.info("Added base model: {}", baseModelId);

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

        // No filled or cooled model generation here anymore
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
     * Get a list of all available texture names that have been found
     * @return List of available texture names
     */
    public List<String> getAvailableTextures() {
        return new ArrayList<>(textureToShapeMap.keySet().stream()
                .filter(textureToShapeMap::get)
                .collect(Collectors.toList()));
    }

    /**
     * Find all texture files in the template directory (classpath)
     *
     * @return List of texture names without extension
     */
    private List<String> findTextureFiles() {
        List<String> textureFiles = new ArrayList<>();

        // Try to list resources from the classpath
        try {
            java.net.URL dirURL = getClass().getClassLoader().getResource(TEMPLATE_PATH);
            if (dirURL != null && dirURL.getProtocol().equals("file")) {
                File dir = new File(dirURL.toURI());
                File[] files = dir.listFiles((d, name) -> name.endsWith(TEXTURE_EXTENSION));
                if (files != null) {
                    for (File file : files) {
                        String name = file.getName();
                        if (name.endsWith(TEXTURE_EXTENSION)) {
                            String textureName = name.substring(0, name.length() - TEXTURE_EXTENSION.length());
                            textureFiles.add(textureName);
                            Forgero.LOGGER.info("Found texture for mold: {}", textureName);
                        }
                    }
                }
            } else if (dirURL != null && dirURL.getProtocol().equals("jar")) {
                String path = TEMPLATE_PATH + "/";
                String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));
                try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarPath)) {
                    java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        String entryName = entries.nextElement().getName();
                        if (entryName.startsWith(path) && entryName.endsWith(TEXTURE_EXTENSION)) {
                            String fileName = entryName.substring(path.length());
                            String textureName = fileName.substring(0, fileName.length() - TEXTURE_EXTENSION.length());
                            textureFiles.add(textureName);
                            Forgero.LOGGER.info("Found texture for mold: {}", textureName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Forgero.LOGGER.warn("Error scanning texture directory in resources: {}", TEMPLATE_PATH);
        }

        // If still no textures found, try using the resource manager
        if (textureFiles.isEmpty() && resourceManager != null) {
            try {
                Map<Identifier, Resource> resources = resourceManager.findResources("templates/textures/molds",
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
    /**
     * Get diagnostics about texture to shape mappings
     * @return A string report of texture mapping status
     */
    public String getTextureMappingDiagnostics() {
        StringBuilder report = new StringBuilder("VoxelShape to Texture Mapping Status:\n");

        int mappedCount = 0;
        int unmappedCount = 0;

        for (Map.Entry<String, Boolean> entry : textureToShapeMap.entrySet()) {
            if (entry.getValue()) {
                mappedCount++;
                report.append("✓ Successfully mapped: ").append(entry.getKey()).append("\n");
            } else {
                unmappedCount++;
                report.append("✗ Failed to map: ").append(entry.getKey()).append("\n");
            }
        }

        report.append("\nSummary: ").append(mappedCount).append(" textures mapped, ")
              .append(unmappedCount).append(" textures failed to map.");

        return report.toString();
    }


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

                    // Improved face culling logic
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
                        "        \"up\": {\"texture\": \"#terracotta\", \"tintindex\": 0},\n" +
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

        // Add walls for border pixels using the same border detection as in createVoxelShapeFromTexture
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (outlinePixels[x][z] && !coloredPixels[x][z]) {
                    // Check if this is a border pixel that needs a wall
                    boolean isBorder = false;

                    // Check direct adjacent cells (N, E, S, W)
                    int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
                    for (int[] dir : directions) {
                        int nx = x + dir[0];
                        int nz = z + dir[1];

                        // It's a border if it's at the edge of the outline or adjacent to a colored pixel
                        if (nx < 0 || nx >= 16 || nz < 0 || nz >= 16 || !outlinePixels[nx][nz] || coloredPixels[nx][nz]) {
                            isBorder = true;
                            break;
                        }
                    }

                    // Always create walls at the edge of the template bounds
                    if (x == 0 || x == 15 || z == 0 || z == 15) {
                        isBorder = true;
                    }

                    if (isBorder) {
                        if (elementsJson.length() > 0) {
                            elementsJson.append(",\n");
                        }

                        // Determine which faces should be rendered based on neighbors
                        boolean showNorth = (z == 0) || !outlinePixels[x][z-1] || coloredPixels[x][z-1];
                        boolean showSouth = (z == 15) || !outlinePixels[x][z+1] || coloredPixels[x][z+1];
                        boolean showEast = (x == 15) || !outlinePixels[x+1][z] || coloredPixels[x+1][z];
                        boolean showWest = (x == 0) || !outlinePixels[x-1][z] || coloredPixels[x-1][z];

                        elementsJson.append(String.format(
                            "    {\n" +
                            "      \"from\": [%d, 1.0, %d],\n" +
                            "      \"to\": [%d, 2.0, %d],\n" +
                            "      \"shade\": true,\n" +
                            "      \"faces\": {\n" +
                            "%s" +
                            "%s" +
                            "%s" +
                            "%s" +
                            "        \"up\": {\"texture\": \"#top\", \"tintindex\": 1}\n" +
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
        }

        return elementsJson.toString();
    }


}
