package com.sigmundgranaas.forgero.smithing.resource;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;

import com.sigmundgranaas.forgero.fabric.resources.dynamic.DynamicResourceGenerator;
import com.sigmundgranaas.forgero.smithing.block.custom.MoldBlock;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.blockstate.JVariant;
import net.devtech.arrp.json.loot.JLootTable;
import net.devtech.arrp.json.tags.JTag;

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
 * Generator for dynamically creating mold blocks based on texture templates.
 * This class processes texture files to create matching block shapes and generates
 * all necessary game resources including models, blockstates, and loot tables.
 */
public class MoldGenerator implements DynamicResourceGenerator {

	// Constants
	private static final String TEMPLATE_PATH = "assets/forgero/templates/textures/molds";
	private static final String TEXTURE_EXTENSION = ".png";
	private static final String MOLD_SUFFIX = "_mold";
	private static final String NAMESPACE = "forgero";
	private static final int GRID_SIZE = 16;
	private static final int GRID_CENTER = 8;
	private static final int ALPHA_THRESHOLD_SHAPE = 30;
	private static final int ALPHA_THRESHOLD_MODEL = 1;
	private static final int OUTLINE_SIZE = 1;
	private static final int BASE_HEIGHT = 1;
	private static final int WALL_HEIGHT = 2;

	// Instance management
	private static MoldGenerator instance;

	// State
	private final Map<String, Block> registeredMoldBlocks = new HashMap<>();
	private final Map<String, String> langEntries = new HashMap<>();
	private ResourceManager resourceManager;

	// Direction vectors for neighbor checking
	private static final int[][] CARDINAL_DIRECTIONS = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
	private static final int[][] ALL_DIRECTIONS = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

	public MoldGenerator() {
		instance = this;
		registerResourceReloadListener();
	}

	public static MoldGenerator getInstance() {
		return instance;
	}

	private void registerResourceReloadListener() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
				.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
					@Override
					public Identifier getFabricId() {
						return new Identifier(NAMESPACE, "mold_textures");
					}

					@Override
					public void reload(ResourceManager resourceManager) {
						MoldGenerator.this.resourceManager = resourceManager;
					}
				});
	}

	public void addMoldBlocksToSmithingTab() {
		if (registeredMoldBlocks.isEmpty()) return;

		ItemGroupEvents.modifyEntriesEvent(
				com.sigmundgranaas.forgero.smithing.ForgeroSmithingInitializer.FORGERO_SMITHING_KEY
		).register(entries -> registeredMoldBlocks.values().forEach(entries::add));
	}

	private Block registerMoldBlock(String textureName) {
		if (registeredMoldBlocks.containsKey(textureName)) {
			return registeredMoldBlocks.get(textureName);
		}

		String moldName = textureName + MOLD_SUFFIX;
		Identifier moldId = new Identifier(NAMESPACE, moldName);

		VoxelShape customShape = createVoxelShapeFromTexture(textureName);
		MoldBlock moldBlock = new MoldBlock(FabricBlockSettings.copyOf(Blocks.STONE_SLAB), customShape);

		// Register block and item
		Registry.register(Registries.BLOCK, moldId, moldBlock);
		Registry.register(Registries.ITEM, moldId, new BlockItem(moldBlock, new FabricItemSettings()));

		// Configure block entity and rendering
		ModBlockEntities.registerMoldBlock(moldBlock);
		BlockRenderLayerMap.INSTANCE.putBlock(moldBlock, RenderLayer.getCutout());

		registeredMoldBlocks.put(textureName, moldBlock);
		return moldBlock;
	}

	private VoxelShape createVoxelShapeFromTexture(String textureName) {
		BufferedImage image = loadTextureImage(textureName);
		if (image == null) {
			return createDefaultShape();
		}

		// 1) Read all pixels (including edges)
		// 2) Center using colored pixels
		// 3) Rebuild outline after centering
		PixelGrid pixelGrid = analyzeTexture(image, ALPHA_THRESHOLD_SHAPE);
		PixelGrid centeredGrid = centerPixelGrid(pixelGrid);

		return buildVoxelShape(centeredGrid);
	}

	private VoxelShape createDefaultShape() {
		return Block.createCuboidShape(0, 0, 0, GRID_SIZE, BASE_HEIGHT, GRID_SIZE);
	}

	private PixelGrid analyzeTexture(BufferedImage image, int alphaThreshold) {
		boolean[][] coloredPixels = new boolean[GRID_SIZE][GRID_SIZE];

		int width = Math.min(image.getWidth(), GRID_SIZE);
		int height = Math.min(image.getHeight(), GRID_SIZE);

		// Read entire image area, including borders
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int pixel = image.getRGB(x, y);
				int alpha = (pixel >> 24) & 0xff;
				if (alpha >= alphaThreshold) {
					coloredPixels[x][y] = true;
				}
			}
		}

		// Outline will be (re)generated after centering
		return new PixelGrid(coloredPixels, new boolean[GRID_SIZE][GRID_SIZE]);
	}

	private boolean[][] createOutline(boolean[][] coloredPixels) {
		boolean[][] outlinePixels = new boolean[GRID_SIZE][GRID_SIZE];

		// Copy original pixels
		for (int x = 0; x < GRID_SIZE; x++) {
			System.arraycopy(coloredPixels[x], 0, outlinePixels[x], 0, GRID_SIZE);
		}

		// Add outline
		for (int x = 0; x < GRID_SIZE; x++) {
			for (int y = 0; y < GRID_SIZE; y++) {
				if (coloredPixels[x][y]) {
					addOutlineAround(outlinePixels, x, y);
				}
			}
		}

		return outlinePixels;
	}

	private void addOutlineAround(boolean[][] outlinePixels, int centerX, int centerY) {
		for (int dx = -OUTLINE_SIZE; dx <= OUTLINE_SIZE; dx++) {
			for (int dy = -OUTLINE_SIZE; dy <= OUTLINE_SIZE; dy++) {
				if (dx == 0 && dy == 0) continue;

				int distance = Math.abs(dx) + Math.abs(dy);
				if (distance <= OUTLINE_SIZE) {
					int nx = centerX + dx;
					int ny = centerY + dy;
					if (isValidGridPosition(nx, ny)) {
						outlinePixels[nx][ny] = true;
					}
				}
			}
		}
	}

	private boolean isValidGridPosition(int x, int y) {
		return x >= 0 && x < GRID_SIZE && y >= 0 && y < GRID_SIZE;
	}

	private PixelGrid centerPixelGrid(PixelGrid grid) {
		// Calculate bounds from colored pixels (not outline) to avoid clipping-induced bias
		BoundingBox bounds = calculateBounds(grid.coloredPixels);
		if (!bounds.isValid()) {
			return grid;
		}

		int dx = GRID_CENTER - bounds.getCenterX();
		int dy = GRID_CENTER - bounds.getCenterY();

		// Shift colored pixels, then rebuild outline to avoid losing edge pixels
		boolean[][] shiftedColored = shiftBooleanGrid(grid.coloredPixels, dx, dy);
		boolean[][] shiftedOutline = createOutline(shiftedColored);

		return new PixelGrid(shiftedColored, shiftedOutline);
	}

	// Shifts a boolean grid by dx, dy inside the 16x16 bounds
	private boolean[][] shiftBooleanGrid(boolean[][] src, int dx, int dy) {
		boolean[][] dst = new boolean[GRID_SIZE][GRID_SIZE];
		for (int x = 0; x < GRID_SIZE; x++) {
			for (int y = 0; y < GRID_SIZE; y++) {
				int sx = x - dx;
				int sy = y - dy;
				if (isValidGridPosition(sx, sy)) {
					dst[x][y] = src[sx][sy];
				}
			}
		}
		return dst;
	}

	private BoundingBox calculateBounds(boolean[][] pixels) {
		int minX = GRID_SIZE, maxX = -1, minY = GRID_SIZE, maxY = -1;

		for (int x = 0; x < GRID_SIZE; x++) {
			for (int y = 0; y < GRID_SIZE; y++) {
				if (pixels[x][y]) {
					minX = Math.min(minX, x);
					maxX = Math.max(maxX, x);
					minY = Math.min(minY, y);
					maxY = Math.max(maxY, y);
				}
			}
		}

		return new BoundingBox(minX, maxX, minY, maxY);
	}

	private PixelGrid shiftPixelGrid(PixelGrid grid, int dx, int dy) {
		boolean[][] shiftedOutline = new boolean[GRID_SIZE][GRID_SIZE];
		boolean[][] shiftedColored = new boolean[GRID_SIZE][GRID_SIZE];

		for (int x = 0; x < GRID_SIZE; x++) {
			for (int y = 0; y < GRID_SIZE; y++) {
				int srcX = x - dx;
				int srcY = y - dy;

				if (isValidGridPosition(srcX, srcY)) {
					shiftedOutline[x][y] = grid.outlinePixels[srcX][srcY];
					shiftedColored[x][y] = grid.coloredPixels[srcX][srcY];
				}
			}
		}

		return new PixelGrid(shiftedColored, shiftedOutline);
	}

	private VoxelShape buildVoxelShape(PixelGrid grid) {
		VoxelShape shape = VoxelShapes.empty();

		// Add base layer
		shape = addBaseLayer(shape, grid.outlinePixels);

		// Add walls
		shape = addWalls(shape, grid);

		return shape;
	}

	private VoxelShape addBaseLayer(VoxelShape shape, boolean[][] outlinePixels) {
		for (int x = 0; x < GRID_SIZE; x++) {
			for (int z = 0; z < GRID_SIZE; z++) {
				if (outlinePixels[x][z]) {
					VoxelShape cube = Block.createCuboidShape(x, 0, z, x + 1, BASE_HEIGHT, z + 1);
					shape = VoxelShapes.union(shape, cube);
				}
			}
		}
		return shape;
	}

	private VoxelShape addWalls(VoxelShape shape, PixelGrid grid) {
		for (int x = 0; x < GRID_SIZE; x++) {
			for (int z = 0; z < GRID_SIZE; z++) {
				if (shouldAddWall(grid, x, z)) {
					VoxelShape wall = Block.createCuboidShape(x, BASE_HEIGHT, z, x + 1, WALL_HEIGHT, z + 1);
					shape = VoxelShapes.union(shape, wall);
				}
			}
		}
		return shape;
	}

	private boolean shouldAddWall(PixelGrid grid, int x, int z) {
		if (!grid.outlinePixels[x][z]) return false;

		// Always add wall at grid edges
		if (x == 0 || x == GRID_SIZE - 1 || z == 0 || z == GRID_SIZE - 1) {
			return true;
		}

		// Add wall if this is outline but not colored (edge of template)
		if (!grid.coloredPixels[x][z]) {
			return hasAdjacentEmptySpace(grid, x, z) || hasAdjacentColoredAtEdge(grid, x, z);
		}

		return false;
	}

	private boolean hasAdjacentEmptySpace(PixelGrid grid, int x, int z) {
		for (int[] dir : CARDINAL_DIRECTIONS) {
			int nx = x + dir[0];
			int nz = z + dir[1];

			if (!isValidGridPosition(nx, nz) || !grid.outlinePixels[nx][nz] || grid.coloredPixels[nx][nz]) {
				return true;
			}
		}
		return false;
	}

	private boolean hasAdjacentColoredAtEdge(PixelGrid grid, int x, int z) {
		for (int[] dir : CARDINAL_DIRECTIONS) {
			int nx = x + dir[0];
			int nz = z + dir[1];

			if (isValidGridPosition(nx, nz) && grid.coloredPixels[nx][nz]) {
				if (nx == 0 || nx == GRID_SIZE - 1 || nz == 0 || nz == GRID_SIZE - 1) {
					return true;
				}
			}
		}
		return false;
	}

	private BufferedImage loadTextureImage(String textureName) {
		// Try loading from classpath first
		BufferedImage image = loadFromClasspath(textureName);
		if (image != null) return image;

		// Try loading from resource manager
		if (resourceManager != null) {
			image = loadFromResourceManager(textureName);
		}

		return image;
	}

	private BufferedImage loadFromClasspath(String textureName) {
		String resourcePath = TEMPLATE_PATH + "/" + textureName + TEXTURE_EXTENSION;
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
			if (is != null) {
				return ImageIO.read(is);
			}
		} catch (IOException e) {
			// Silently ignore - will try other methods
		}
		return null;
	}

	private BufferedImage loadFromResourceManager(String textureName) {
		try {
			Identifier textureId = new Identifier(NAMESPACE, "templates/textures/molds/" + textureName + TEXTURE_EXTENSION);
			Optional<Resource> resource = resourceManager.getResource(textureId);

			if (resource.isPresent()) {
				try (InputStream is = resource.get().getInputStream()) {
					return ImageIO.read(is);
				}
			}
		} catch (IOException e) {
			// Silently ignore
		}
		return null;
	}

	private void generateMoldBlockResources(RuntimeResourcePack pack, String textureName, Block block) {
		String moldName = textureName + MOLD_SUFFIX;
		Identifier moldId = new Identifier(NAMESPACE, moldName);

		generateLootTable(pack, moldId);
		generateBlockState(pack, moldId);
		generateModels(pack, moldId, textureName);
		generateTags(pack, moldId);
	}

	private void generateLootTable(RuntimeResourcePack pack, Identifier moldId) {
		JLootTable lootTable = JLootTable.loot("minecraft:block")
				.pool(JLootTable.pool()
						.rolls(1)
						.entry(JLootTable.entry()
								.type("minecraft:item")
								.name(moldId.toString()))
						.condition(JLootTable.predicate("minecraft:survives_explosion")));

		pack.addLootTable(new Identifier(moldId.getNamespace(), "blocks/" + moldId.getPath()), lootTable);
	}

	private void generateBlockState(RuntimeResourcePack pack, Identifier moldId) {
		String baseModelId = moldId.getNamespace() + ":block/" + moldId.getPath();
		JVariant variant = JState.variant();

		// Generate variants for all fill states and progress levels
		for (int i = 0; i <= 100; i++) {
			variant.put("filled=false,progress=" + i, JState.model(baseModelId));
			variant.put("filled=true,progress=" + i, JState.model(baseModelId));
		}

		JState blockState = JState.state().add(variant);
		pack.addBlockState(blockState, new Identifier(moldId.getNamespace(), moldId.getPath()));
	}

	private void generateModels(RuntimeResourcePack pack, Identifier moldId, String textureName) {
		BufferedImage image = loadTextureImage(textureName);
		if (image == null) return;

		// Use same corrected pipeline for models
		PixelGrid grid = analyzeTexture(image, ALPHA_THRESHOLD_MODEL);
		grid = centerPixelGrid(grid);

		String blockModelJson = generateBlockModel(grid);
		String itemModelJson = generateItemModel(moldId);

		// Add models to pack
		Identifier blockModelId = new Identifier(moldId.getNamespace(), "models/block/" + moldId.getPath() + ".json");
		Identifier itemModelId = new Identifier(moldId.getNamespace(), "models/item/" + moldId.getPath() + ".json");

		pack.addAsset(blockModelId, blockModelJson.getBytes(StandardCharsets.UTF_8));
		pack.addAsset(itemModelId, itemModelJson.getBytes(StandardCharsets.UTF_8));
	}

	private String generateBlockModel(PixelGrid grid) {
		StringBuilder elements = new StringBuilder();

		// Generate base elements
		addBaseElements(elements, grid.outlinePixels);

		// Generate wall elements
		addWallElements(elements, grid);

		return String.format(
				"{\n" +
						"  \"parent\": \"minecraft:block/block\",\n" +
						"  \"ambientocclusion\": true,\n" +
						"  \"textures\": {\n" +
						"    \"terracotta\": \"forgero:block/terracotta\",\n" +
						"    \"particle\": \"forgero:block/terracotta\"\n" +
						"  },\n" +
						"  \"elements\": [\n%s\n  ]\n" +
						"}", elements.toString());
	}

	private void addBaseElements(StringBuilder elements, boolean[][] outlinePixels) {
		for (int x = 0; x < GRID_SIZE; x++) {
			for (int z = 0; z < GRID_SIZE; z++) {
				if (outlinePixels[x][z]) {
					if (elements.length() > 0) elements.append(",\n");
					elements.append(createBaseElement(x, z, outlinePixels));
				}
			}
		}
	}

	private String createBaseElement(int x, int z, boolean[][] outlinePixels) {
		boolean showNorth = z == 0 || !outlinePixels[x][z - 1];
		boolean showSouth = z == GRID_SIZE - 1 || !outlinePixels[x][z + 1];
		boolean showEast = x == GRID_SIZE - 1 || !outlinePixels[x + 1][z];
		boolean showWest = x == 0 || !outlinePixels[x - 1][z];

		return String.format(
				"    {\n" +
						"      \"from\": [%d, 0.0, %d],\n" +
						"      \"to\": [%d, %d.0, %d],\n" +
						"      \"faces\": {\n" +
						"%s%s%s%s" +
						"        \"up\": {\"texture\": \"#terracotta\"},\n" +
						"        \"down\": {\"texture\": \"#terracotta\", \"cullface\": \"down\"}\n" +
						"      }\n" +
						"    }",
				x, z, x + 1, BASE_HEIGHT, z + 1,
				showNorth ? createFaceString("north", z == 0) : "",
				showEast ? createFaceString("east", x == GRID_SIZE - 1) : "",
				showSouth ? createFaceString("south", z == GRID_SIZE - 1) : "",
				showWest ? createFaceString("west", x == 0) : ""
		);
	}

	private String createFaceString(String face, boolean cullface) {
		return String.format("        \"%s\": {\"texture\": \"#terracotta\"%s},\n",
				face, cullface ? ", \"cullface\": \"" + face + "\"" : "");
	}

	private void addWallElements(StringBuilder elements, PixelGrid grid) {
		for (int x = 0; x < GRID_SIZE; x++) {
			for (int z = 0; z < GRID_SIZE; z++) {
				if (shouldAddWallElement(grid, x, z)) {
					if (elements.length() > 0) elements.append(",\n");
					elements.append(createWallElement(x, z));
				}
			}
		}
	}

	private boolean shouldAddWallElement(PixelGrid grid, int x, int z) {
		if (!grid.outlinePixels[x][z] || grid.coloredPixels[x][z]) return false;

		// Check if this position is at an edge
		for (int[] dir : ALL_DIRECTIONS) {
			int nx = x + dir[0];
			int nz = z + dir[1];

			if (!isValidGridPosition(nx, nz) || !grid.outlinePixels[nx][nz]) {
				return true;
			}
		}

		return false;
	}

	private String createWallElement(int x, int z) {
		return String.format(
				"    {\n" +
						"      \"from\": [%d, %d.0, %d],\n" +
						"      \"to\": [%d, %d.0, %d],\n" +
						"      \"shade\": true,\n" +
						"      \"faces\": {\n" +
						"        \"north\": {\"texture\": \"#terracotta\"%s},\n" +
						"        \"east\": {\"texture\": \"#terracotta\"%s},\n" +
						"        \"south\": {\"texture\": \"#terracotta\"%s},\n" +
						"        \"west\": {\"texture\": \"#terracotta\"%s},\n" +
						"        \"up\": {\"texture\": \"#terracotta\"}\n" +
						"      }\n" +
						"    }",
				x, BASE_HEIGHT, z, x + 1, WALL_HEIGHT, z + 1,
				z == 0 ? ", \"cullface\": \"north\"" : "",
				x == GRID_SIZE - 1 ? ", \"cullface\": \"east\"" : "",
				z == GRID_SIZE - 1 ? ", \"cullface\": \"south\"" : "",
				x == 0 ? ", \"cullface\": \"west\"" : ""
		);
	}

	private String generateItemModel(Identifier moldId) {
		return String.format(
				"{\n  \"parent\": \"%s:block/%s\"\n}",
				moldId.getNamespace(), moldId.getPath()
		);
	}

	private void generateTags(RuntimeResourcePack pack, Identifier moldId) {
		addTag(pack, new Identifier("minecraft", "mineable/pickaxe"), moldId);
		addTag(pack, new Identifier("minecraft", "needs_stone_tool"), moldId);
		addTag(pack, new Identifier(NAMESPACE, "items/molds"), moldId);
	}

	private void addTag(RuntimeResourcePack pack, Identifier tagId, Identifier entryId) {
		JTag tag = JTag.tag().add(entryId);
		pack.addTag(tagId, tag);
	}

	private List<String> findTextureFiles() {
		List<String> textureFiles = new ArrayList<>();

		// Try loading from classpath
		textureFiles.addAll(findTexturesFromClasspath());

		// Try loading from resource manager if classpath failed
		if (textureFiles.isEmpty() && resourceManager != null) {
			textureFiles.addAll(findTexturesFromResourceManager());
		}

		return textureFiles;
	}

	private List<String> findTexturesFromClasspath() {
		List<String> textureFiles = new ArrayList<>();

		try {
			URL dirURL = getClass().getClassLoader().getResource(TEMPLATE_PATH);
			if (dirURL == null) return textureFiles;

			if ("file".equals(dirURL.getProtocol())) {
				textureFiles.addAll(findTexturesFromFileSystem(dirURL));
			} else if ("jar".equals(dirURL.getProtocol())) {
				textureFiles.addAll(findTexturesFromJar(dirURL));
			}
		} catch (Exception e) {
			// Silently ignore
		}

		return textureFiles;
	}

	private List<String> findTexturesFromFileSystem(URL dirURL) throws URISyntaxException {
		List<String> textureFiles = new ArrayList<>();

		java.io.File dir = new java.io.File(dirURL.toURI());
		java.io.File[] files = dir.listFiles((d, name) -> name.endsWith(TEXTURE_EXTENSION));

		if (files != null) {
			for (java.io.File file : files) {
				String name = file.getName();
				String textureName = name.substring(0, name.length() - TEXTURE_EXTENSION.length());
				textureFiles.add(textureName);
			}
		}

		return textureFiles;
	}

	private List<String> findTexturesFromJar(URL dirURL) throws IOException {
		List<String> textureFiles = new ArrayList<>();

		String path = TEMPLATE_PATH + "/";
		String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!"));

		try (JarFile jar = new JarFile(jarPath)) {
			jar.entries().asIterator().forEachRemaining(entry -> {
				String entryName = entry.getName();
				if (entryName.startsWith(path) && entryName.endsWith(TEXTURE_EXTENSION)) {
					String fileName = entryName.substring(path.length());
					String textureName = fileName.substring(0, fileName.length() - TEXTURE_EXTENSION.length());
					textureFiles.add(textureName);
				}
			});
		}

		return textureFiles;
	}

	private List<String> findTexturesFromResourceManager() {
		List<String> textureFiles = new ArrayList<>();

		try {
			Map<Identifier, Resource> resources = resourceManager.findResources(
					"templates/textures/molds",
					id -> id.getPath().endsWith(TEXTURE_EXTENSION)
			);

			for (Identifier id : resources.keySet()) {
				String path = id.getPath();
				String fileName = path.substring(path.lastIndexOf('/') + 1);
				String textureName = fileName.substring(0, fileName.length() - TEXTURE_EXTENSION.length());
				textureFiles.add(textureName);
			}
		} catch (Exception e) {
			// Silently ignore
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
			} catch (Exception e) {
				// Log error but continue processing other textures
				System.err.println("Failed to generate mold for texture: " + textureName + " - " + e.getMessage());
			}
		}

		addMoldBlocksToSmithingTab();
		ModBlockEntities.rebuildMoldBlockEntityType();
	}

	// Helper classes
	private static class PixelGrid {
		final boolean[][] coloredPixels;
		final boolean[][] outlinePixels;

		PixelGrid(boolean[][] coloredPixels, boolean[][] outlinePixels) {
			this.coloredPixels = coloredPixels;
			this.outlinePixels = outlinePixels;
		}
	}

	private static class BoundingBox {
		private final int minX, maxX, minY, maxY;

		BoundingBox(int minX, int maxX, int minY, int maxY) {
			this.minX = minX;
			this.maxX = maxX;
			this.minY = minY;
			this.maxY = maxY;
		}

		boolean isValid() {
			return maxX >= minX && maxY >= minY;
		}

		int getCenterX() {
			return minX + (maxX - minX + 1) / 2;
		}

		int getCenterY() {
			return minY + (maxY - minY + 1) / 2;
		}
	}
}
