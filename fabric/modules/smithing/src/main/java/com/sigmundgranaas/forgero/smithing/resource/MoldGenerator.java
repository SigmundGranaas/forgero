package com.sigmundgranaas.forgero.smithing.resource;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sigmundgranaas.forgero.fabric.resources.dynamic.DynamicResourceGenerator;
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

public class MoldGenerator implements DynamicResourceGenerator {
    private static final String TEMPLATE_PATH = "assets/forgero/templates/textures/molds";
    private static final String TEXTURE_EXTENSION = ".png";
    private final Map<String, Block> registeredMoldBlocks = new HashMap<>();
    private ResourceManager resourceManager;
    private final Map<String, Boolean> textureToShapeMap = new HashMap<>();
    private static MoldGenerator INSTANCE;

    public MoldGenerator() {
        INSTANCE = this;
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

    public static MoldGenerator getInstance() {
        return INSTANCE;
    }

    public void addMoldBlocksToSmithingTab() {
        if (registeredMoldBlocks.isEmpty()) return;
        ItemGroupEvents.modifyEntriesEvent(com.sigmundgranaas.forgero.smithing.ForgeroSmithingInitializer.FORGERO_SMITHING_KEY).register(entries -> {
            for (Block block : registeredMoldBlocks.values()) {
                entries.add(block);
            }
        });
    }

    private Block registerMoldBlock(String textureName) {
        String moldName = textureName + "_mold";
        Identifier moldId = new Identifier("forgero", moldName);
        if (registeredMoldBlocks.containsKey(textureName)) {
            return registeredMoldBlocks.get(textureName);
        }
        VoxelShape customShape = createVoxelShapeFromTexture(textureName);
        MoldBlock moldBlock = new MoldBlock(FabricBlockSettings.copyOf(Blocks.STONE_SLAB), customShape);
        Registry.register(Registries.BLOCK, moldId, moldBlock);
        Registry.register(Registries.ITEM, moldId, new BlockItem(moldBlock, new FabricItemSettings()));
        ModBlockEntities.registerMoldBlock(moldBlock);
        BlockRenderLayerMap.INSTANCE.putBlock(moldBlock, RenderLayer.getCutout());
        registeredMoldBlocks.put(textureName, moldBlock);
        return moldBlock;
    }

    private VoxelShape createVoxelShapeFromTexture(String textureName) {
        BufferedImage originalImage = loadTextureImage(textureName);
        if (originalImage == null) {
            return Block.createCuboidShape(0, 0, 0, 16, 2, 16);
        }
        boolean[][] coloredPixels = new boolean[16][16];
        int width = Math.min(originalImage.getWidth(), 16);
        int height = Math.min(originalImage.getHeight(), 16);
        int alphaThreshold = 30;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) continue;
                int pixel = originalImage.getRGB(x, y);
                int alpha = (pixel >> 24) & 0xff;
                if (alpha >= alphaThreshold) {
                    coloredPixels[x][y] = true;
                }
            }
        }
        boolean[][] outlinePixels = new boolean[16][16];
        for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) if (coloredPixels[x][y]) outlinePixels[x][y] = true;
        int outlineSize = 1;
        for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) if (coloredPixels[x][y])
            for (int dx = -outlineSize; dx <= outlineSize; dx++)
                for (int dy = -outlineSize; dy <= outlineSize; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int distance = Math.abs(dx) + Math.abs(dy);
                    if (distance <= outlineSize) {
                        int nx = x + dx, ny = y + dy;
                        if (nx >= 0 && nx < 16 && ny >= 0 && ny < 16) outlinePixels[nx][ny] = true;
                    }
                }
        int minX = 16, maxX = -1, minZ = 16, maxZ = -1;
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) if (outlinePixels[x][z]) {
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
        }
        int dx = 0, dz = 0;
        if (maxX >= minX && maxZ >= minZ) {
            int shapeWidth = maxX - minX + 1, shapeDepth = maxZ - minZ + 1;
            int centerX = minX + shapeWidth / 2, centerZ = minZ + shapeDepth / 2, gridCenter = 8;
            dx = gridCenter - centerX; dz = gridCenter - centerZ;
        }
        boolean[][] shiftedOutline = new boolean[16][16], shiftedColored = new boolean[16][16];
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            int sx = x + dx, sz = z + dz;
            if (sx >= 0 && sx < 16 && sz >= 0 && sz < 16) {
                shiftedOutline[sx][sz] = outlinePixels[x][z];
                shiftedColored[sx][sz] = coloredPixels[x][z];
            }
        }
        outlinePixels = shiftedOutline;
        coloredPixels = shiftedColored;
        VoxelShape finalShape = VoxelShapes.empty();
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) if (outlinePixels[x][z])
            finalShape = VoxelShapes.union(finalShape, Block.createCuboidShape(x, 0, z, x + 1, 1, z + 1));
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            boolean shouldAddWall = false;
            if (outlinePixels[x][z] && (x == 0 || x == 15 || z == 0 || z == 15)) shouldAddWall = true;
            else if (outlinePixels[x][z] && !coloredPixels[x][z]) {
                boolean isAtTemplateEdge = (x == 0 || x == 15 || z == 0 || z == 15);
                if (isAtTemplateEdge) shouldAddWall = true;
                else {
                    int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
                    for (int[] dir : directions) {
                        int nx = x + dir[0], nz = z + dir[1];
                        if (nx < 0 || nx >= 16 || nz < 0 || nz >= 16 || !outlinePixels[nx][nz] || coloredPixels[nx][nz]) {
                            shouldAddWall = true; break;
                        }
                    }
                }
            }
            if (shouldAddWall)
                finalShape = VoxelShapes.union(finalShape, Block.createCuboidShape(x, 1, z, x + 1, 2, z + 1));
        }
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++)
            if (outlinePixels[x][z] && !coloredPixels[x][z]) {
                boolean needsEdgeWall = false;
                if (x == 0 || x == 15 || z == 0 || z == 15) needsEdgeWall = true;
                int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
                for (int[] dir : directions) {
                    int nx = x + dir[0], nz = z + dir[1];
                    if (nx >= 0 && nx < 16 && nz >= 0 && nz < 16 && coloredPixels[nx][nz])
                        if (nx == 0 || nx == 15 || nz == 0 || nz == 15) { needsEdgeWall = true; break; }
                }
                if (needsEdgeWall)
                    finalShape = VoxelShapes.union(finalShape, Block.createCuboidShape(x, 1, z, x + 1, 2, z + 1));
            }
        textureToShapeMap.put(textureName, true);
        return finalShape;
    }

    private BufferedImage loadTextureImage(String textureName) {
        BufferedImage originalImage = null;
        String resourcePath = TEMPLATE_PATH + "/" + textureName + TEXTURE_EXTENSION;
        try (java.io.InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is != null) {
                originalImage = javax.imageio.ImageIO.read(is);
            }
        } catch (IOException ignored) {}
        if (originalImage == null && resourceManager != null) {
            try {
                Identifier textureId = new Identifier("forgero", "templates/textures/molds/" + textureName + TEXTURE_EXTENSION);
                Resource resource = resourceManager.getResource(textureId).orElse(null);
                if (resource != null) {
                    try (java.io.InputStream is = resource.getInputStream()) {
                        originalImage = javax.imageio.ImageIO.read(is);
                    }
                }
            } catch (IOException ignored) {}
        }
        return originalImage;
    }

    private void generateMoldBlockResources(RuntimeResourcePack pack, String textureName, Block block) {
        String moldName = textureName + "_mold";
        Identifier moldId = new Identifier("forgero", moldName);
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
        String baseModelId = moldId.getNamespace() + ":block/" + moldId.getPath();
        JVariant variant = JState.variant();
        for (int i = 0; i <= 100; i++) {
            variant.put("filled=false,progress=" + i, JState.model(baseModelId));
            variant.put("filled=true,progress=" + i, JState.model(baseModelId));
        }
        JState blockState = JState.state().add(variant);
        pack.addBlockState(blockState, new Identifier(moldId.getNamespace(), moldId.getPath()));
        generateBasicModels(pack, moldId, textureName);
        addTag(pack, new Identifier("minecraft", "mineable/pickaxe"), moldId);
        addTag(pack, new Identifier("minecraft", "needs_stone_tool"), moldId);
        addTag(pack, new Identifier("forgero", "items/molds"), moldId);
    }

    private void generateBasicModels(RuntimeResourcePack pack, Identifier moldId, String textureName) {
        BufferedImage originalImage = loadTextureImage(textureName);
        if (originalImage == null) return;
        boolean[][] coloredPixels = new boolean[16][16];
        int width = Math.min(originalImage.getWidth(), 16), height = Math.min(originalImage.getHeight(), 16);
        int alphaThreshold = 1;
        for (int x = 0; x < width; x++) for (int y = 0; y < height; y++) {
            if (x == 0 || x == width - 1 || y == 0 || y == height - 1) continue;
            int pixel = originalImage.getRGB(x, y);
            int alpha = (pixel >> 24) & 0xff;
            if (alpha >= alphaThreshold) coloredPixels[x][y] = true;
        }
        boolean[][] outlinePixels = new boolean[16][16];
        for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) if (coloredPixels[x][y]) outlinePixels[x][y] = true;
        int outlineSize = 1;
        for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) if (coloredPixels[x][y])
            for (int dx = -outlineSize; dx <= outlineSize; dx++)
                for (int dy = -outlineSize; dy <= outlineSize; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    int distance = Math.abs(dx) + Math.abs(dy);
                    if (distance <= outlineSize) {
                        int nx = x + dx, ny = y + dy;
                        if (nx >= 0 && nx < 16 && ny >= 0 && ny < 16) outlinePixels[nx][ny] = true;
                    }
                }
        int minX = 16, maxX = -1, minZ = 16, maxZ = -1;
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) if (outlinePixels[x][z]) {
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (z < minZ) minZ = z;
            if (z > maxZ) maxZ = z;
        }
        int dx = 0, dz = 0;
        if (maxX >= minX && maxZ >= minZ) {
            int shapeWidth = maxX - minX + 1, shapeDepth = maxZ - minZ + 1;
            int centerX = minX + shapeWidth / 2, centerZ = minZ + shapeDepth / 2, gridCenter = 8;
            dx = gridCenter - centerX; dz = gridCenter - centerZ;
        }
        boolean[][] shiftedOutline = new boolean[16][16], shiftedColored = new boolean[16][16];
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            int sx = x + dx, sz = z + dz;
            if (sx >= 0 && sx < 16 && sz >= 0 && sz < 16) {
                shiftedOutline[sx][sz] = outlinePixels[x][z];
                shiftedColored[sx][sz] = coloredPixels[x][z];
            }
        }
        outlinePixels = shiftedOutline;
        coloredPixels = shiftedColored;
        StringBuilder elementsJson = new StringBuilder();
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) if (outlinePixels[x][z]) {
            if (elementsJson.length() > 0) elementsJson.append(",\n");
            boolean showNorth = z == 0 || !outlinePixels[x][z-1];
            boolean showSouth = z == 15 || !outlinePixels[x][z+1];
            boolean showEast = x == 15 || !outlinePixels[x+1][z];
            boolean showWest = x == 0 || !outlinePixels[x-1][z];
            elementsJson.append(String.format(
                "    {\n" +
                "      \"from\": [%d, 0.0, %d],\n" +
                "      \"to\": [%d, 1.0, %d],\n" +
                "      \"faces\": {\n" +
                "%s%s%s%s" +
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
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++)
            if (outlinePixels[x][z] && !coloredPixels[x][z]) {
                boolean isEdge = false;
                for (int dx2 = -1; dx2 <= 1; dx2++) for (int dz2 = -1; dz2 <= 1; dz2++) {
                    if (dx2 == 0 && dz2 == 0) continue;
                    int nx = x + dx2, nz = z + dz2;
                    if (nx < 0 || nx >= 16 || nz < 0 || nz >= 16 || !outlinePixels[nx][nz]) { isEdge = true; break; }
                }
                if (isEdge) {
                    if (elementsJson.length() > 0) elementsJson.append(",\n");
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
        Identifier baseModelId = new Identifier(moldId.getNamespace(), "models/block/" + moldId.getPath() + ".json");
        pack.addAsset(baseModelId, baseModelJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String itemModelJson = String.format(
            "{\n" +
            "  \"parent\": \"%s:block/%s\"\n" +
            "}",
            moldId.getNamespace(), moldId.getPath()
        );
        Identifier itemModelId = new Identifier(moldId.getNamespace(), "models/item/" + moldId.getPath() + ".json");
        pack.addAsset(itemModelId, itemModelJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private void addTag(RuntimeResourcePack pack, Identifier tagId, Identifier entryId) {
        net.devtech.arrp.json.tags.JTag tag = net.devtech.arrp.json.tags.JTag.tag();
        tag.add(entryId);
        pack.addTag(tagId, tag);
    }

    private List<String> findTextureFiles() {
        List<String> textureFiles = new ArrayList<>();
        try {
            java.net.URL dirURL = getClass().getClassLoader().getResource(TEMPLATE_PATH);
            if (dirURL != null && dirURL.getProtocol().equals("file")) {
                java.io.File dir = new java.io.File(dirURL.toURI());
                java.io.File[] files = dir.listFiles((d, name) -> name.endsWith(TEXTURE_EXTENSION));
                if (files != null) for (java.io.File file : files) {
                    String name = file.getName();
                    if (name.endsWith(TEXTURE_EXTENSION)) {
                        String textureName = name.substring(0, name.length() - TEXTURE_EXTENSION.length());
                        textureFiles.add(textureName);
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
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        if (textureFiles.isEmpty() && resourceManager != null) {
            try {
                Map<Identifier, Resource> resources = resourceManager.findResources("templates/textures/molds",
                        id -> id.getPath().endsWith(TEXTURE_EXTENSION));
                for (Identifier id : resources.keySet()) {
                    String path = id.getPath();
                    if (path.endsWith(TEXTURE_EXTENSION)) {
                        String fileName = path.substring(path.lastIndexOf('/') + 1);
                        String textureName = fileName.substring(0, fileName.length() - TEXTURE_EXTENSION.length());
                        textureFiles.add(textureName);
                    }
                }
            } catch (Exception ignored) {}
        }
        return textureFiles;
    }

    @Override
    public void generate(RuntimeResourcePack pack) {
        List<String> textureNames = findTextureFiles();
        if (textureNames.isEmpty()) return;
        for (String textureName : textureNames) {
            try {
                Block moldBlock = registerMoldBlock(textureName);
                generateMoldBlockResources(pack, textureName, moldBlock);
            } catch (Exception ignored) {}
        }
        addMoldBlocksToSmithingTab();
        ModBlockEntities.rebuildMoldBlockEntityType();
    }
}
