package com.sigmundgranaas.forgero.smithing.resource;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.fabric.resources.dynamic.DynamicResourceGenerator;
import com.sigmundgranaas.forgero.smithing.ForgeroSmithingInitializer;
import com.sigmundgranaas.forgero.smithing.block.custom.MoldBlock;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.blockstate.JVariant;
import net.devtech.arrp.json.loot.JLootTable;
import net.devtech.arrp.json.models.JModel;

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

	// Debug flag to print detailed path information
	private static final boolean DEBUG_PATH = true;

	private final Map<String, Block> registeredMoldBlocks = new HashMap<>();
	private final Map<String, BufferedImage> processedTextures = new HashMap<>();
	private ResourceManager resourceManager;

	// Singleton instance
	private static MoldGenerator INSTANCE;

	public MoldGenerator() {
		INSTANCE = this;

		// Print debug info about paths
		if (DEBUG_PATH) {
			Forgero.LOGGER.info("DEV_TEXTURE_PATH: {}", DEV_TEXTURE_PATH);
			Forgero.LOGGER.info("ALT_TEXTURE_PATH: {}", ALT_TEXTURE_PATH);
			File devDir = new File(DEV_TEXTURE_PATH);
			File altDir = new File(ALT_TEXTURE_PATH);
			Forgero.LOGGER.info("DEV_TEXTURE_PATH exists: {}", devDir.exists());
			Forgero.LOGGER.info("ALT_TEXTURE_PATH exists: {}", altDir.exists());

			// Try additional paths
			for (String additionalPath : ADDITIONAL_TEXTURE_PATHS) {
				Path resolvedPath = FabricLoader.getInstance().getGameDir().resolve(additionalPath).normalize();
				File dir = resolvedPath.toFile();
				Forgero.LOGGER.info("Trying additional path: {}, exists: {}", resolvedPath, dir.exists());

				if (dir.exists() && dir.isDirectory()) {
					File[] files = dir.listFiles((d, name) -> name.endsWith(TEXTURE_EXTENSION));
					if (files != null && files.length > 0) {
						Forgero.LOGGER.info("Found {} texture files in: {}", files.length, resolvedPath);
						for (File file : files) {
							Forgero.LOGGER.info("Found texture file: {}", file.getName());
						}
					}
				}
			}

			if (devDir.exists()) {
				File[] files = devDir.listFiles();
				if (files != null) {
					Forgero.LOGGER.info("DEV_TEXTURE_PATH contains {} files", files.length);
					for (File file : files) {
						Forgero.LOGGER.info("Found texture file: {}", file.getName());
					}
				}
			}
		}

		// Register a resource reload listener to get access to the ResourceManager
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return new Identifier("forgero", "mold_textures");
			}

			@Override
			public void reload(ResourceManager resourceManager) {
				MoldGenerator.this.resourceManager = resourceManager;
				// Clear cached textures when resources are reloaded
				processedTextures.clear();
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

		// Use Fabric API's ItemGroupEvents to add items to the smithing tab using the correct RegistryKey
		ItemGroupEvents.modifyEntriesEvent(ForgeroSmithingInitializer.FORGERO_SMITHING_KEY).register(entries -> {
			for (Block block : registeredMoldBlocks.values()) {
				entries.add(block);
				Forgero.LOGGER.debug("Added mold block to smithing tab: {}", Registries.BLOCK.getId(block));
			}
		});
	}

	/**
	 * Process a texture to create a hollow mold version
	 *
	 * @param textureName The texture name without extension
	 * @return The processed image with an outline and hollow interior
	 */
	private BufferedImage processTextureForMold(String textureName) {
		BufferedImage originalImage = null;
		String sourcePath = null;

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
					originalImage = ImageIO.read(file);
					sourcePath = file.getAbsolutePath();
					Forgero.LOGGER.info("Loaded texture {} from: {}", textureName, sourcePath);
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
					try (InputStream is = resource.getInputStream()) {
						originalImage = ImageIO.read(is);
						sourcePath = textureId.toString();
					}
				}
			} catch (IOException e) {
				Forgero.LOGGER.warn("Error reading texture from resource manager: {}", textureName);
			}
		}

		if (originalImage == null) {
			Forgero.LOGGER.error("Failed to load texture: {}", textureName);
			return null;
		}

		Forgero.LOGGER.info("Processing texture '{}' from source: {}", textureName, sourcePath);

		// Create a new image with the same dimensions but ensure ARGB type for transparency
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		BufferedImage moldImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		// Initialize with transparent pixels instead of copying the original image
		// This ensures all non-colored pixels start as transparent
		int transparentColor = 0x00000000; // Fully transparent
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				moldImage.setRGB(x, y, transparentColor);
			}
		}

		// Keep track of original colored pixels
		boolean[][] hasColoredPixel = new boolean[width][height];
		boolean[][] isOutlinePixel = new boolean[width][height];

		// Identify non-transparent pixels in the original image
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int pixel = originalImage.getRGB(x, y);
				int alpha = (pixel >> 24) & 0xff;
				if (alpha > 10) { // Allow for slightly transparent pixels
					hasColoredPixel[x][y] = true;
					// Copy colored pixels from original to mold image
					moldImage.setRGB(x, y, pixel);
				}
			}
		}

		// Create a set to store coordinates of outline pixels
		Set<int[]> outlinePixels = new java.util.HashSet<>();

		// Identify outline pixels (adjacent to colored pixels but not colored themselves)
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (hasColoredPixel[x][y]) {
					// Check the 4 adjacent pixels (not diagonals)
					int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}}; // up, right, down, left

					for (int[] dir : directions) {
						int nx = x + dir[0];
						int ny = y + dir[1];

						if (nx >= 0 && nx < width && ny >= 0 && ny < height && !hasColoredPixel[nx][ny]) {
							// This is a transparent pixel adjacent to a colored pixel
							outlinePixels.add(new int[]{nx, ny});
							isOutlinePixel[nx][ny] = true;
						}
					}
				}
			}
		}

		// Apply outline color to the outline pixels
		for (int[] coords : outlinePixels) {
			int x = coords[0];
			int y = coords[1];

			// Find the closest colored pixel to determine the outline color
			int closestColor = findClosestColoredPixel(originalImage, hasColoredPixel, x, y);

			// Darken the color for the outline, but not too much to avoid black
			int alpha = (closestColor >> 24) & 0xff;
			int r = (closestColor >> 16) & 0xff;
			int g = (closestColor >> 8) & 0xff;
			int b = closestColor & 0xff;

			// Use a less aggressive darkening factor (0.8 instead of 0.7)
			// This prevents colors from becoming too dark/black
			r = Math.max(20, (int) (r * 0.8));
			g = Math.max(20, (int) (g * 0.8));
			b = Math.max(20, (int) (b * 0.8));

			int outlineColor = (alpha << 24) | (r << 16) | (g << 8) | b;
			moldImage.setRGB(x, y, outlineColor);
		}

		// All pixels that aren't part of the original texture or the outline remain transparent
		// We don't need to do anything else since we started with a transparent image

		// Handle special case for TYPE_BYTE_BINARY or other indexed images
		if (originalImage.getType() == BufferedImage.TYPE_BYTE_BINARY ||
				originalImage.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
			// Convert to a more compatible format - but keep transparency
			BufferedImage convertedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			convertedImage.getGraphics().drawImage(moldImage, 0, 0, null);
			moldImage = convertedImage;
		}

		// Make sure the directories exist for the generated textures
		try {
			// Create a directory to ensure the structure exists
			File debugOutputDir = new File("debug_textures/block/molds");
			if (!debugOutputDir.exists()) {
				debugOutputDir.mkdirs();
			}
			// Save the texture for debugging
			ImageIO.write(moldImage, "png", new File(debugOutputDir, textureName + ".png"));
			Forgero.LOGGER.info("Debug texture saved to: {}", new File(debugOutputDir, textureName + ".png").getAbsolutePath());
		} catch (Exception e) {
			Forgero.LOGGER.warn("Failed to save debug texture: {}", e.getMessage());
		}

		return moldImage;
	}

	/**
	 * Find the closest colored pixel to determine the color for an outline pixel
	 *
	 * @param originalImage   The original image
	 * @param hasColoredPixel Map of which pixels are colored
	 * @param x               X coordinate
	 * @param y               Y coordinate
	 * @return RGB color value of the closest colored pixel
	 */
	private int findClosestColoredPixel(BufferedImage originalImage, boolean[][] hasColoredPixel, int x, int y) {
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		// We'll prioritize the direct neighbors (up, right, down, left)
		int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}}; // up, right, down, left

		for (int[] dir : directions) {
			int nx = x + dir[0];
			int ny = y + dir[1];

			if (nx >= 0 && nx < width && ny >= 0 && ny < height && hasColoredPixel[nx][ny]) {
				return originalImage.getRGB(nx, ny);
			}
		}

		// If no direct neighbors, try diagonals
		int[][] diagonals = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}}; // top-left, top-right, bottom-right, bottom-left

		for (int[] dir : diagonals) {
			int nx = x + dir[0];
			int ny = y + dir[1];

			if (nx >= 0 && nx < width && ny >= 0 && ny < height && hasColoredPixel[nx][ny]) {
				return originalImage.getRGB(nx, ny);
			}
		}

		// If still not found, use a default color (dark gray)
		return 0xFF555555;
	}

	/**
	 * Analyze a texture to determine the bounds of the colored pixels
	 *
	 * @param textureName The texture name to analyze
	 * @return int array with [minX, minY, maxX, maxY] or null if texture not found
	 */
	private int[] analyzeTextureBounds(String textureName) {
		BufferedImage originalImage = null;

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
					originalImage = ImageIO.read(file);
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
					try (InputStream is = resource.getInputStream()) {
						originalImage = ImageIO.read(is);
					}
				}
			} catch (IOException e) {
				Forgero.LOGGER.warn("Error reading texture from resource manager: {}", textureName);
			}
		}

		if (originalImage == null) {
			return null;
		}

		// Initialize bounds to extreme values
		int minX = originalImage.getWidth();
		int minY = originalImage.getHeight();
		int maxX = 0;
		int maxY = 0;

		// Scan for colored pixels
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

		// Create and register the block
		Block moldBlock = Registry.register(
				Registries.BLOCK,
				moldId,
				new MoldBlock(FabricBlockSettings.copyOf(Blocks.STONE_SLAB))
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

		Forgero.LOGGER.info("Registered mold block with block entity support: {}", moldId);

		// Add to our tracking map
		registeredMoldBlocks.put(textureName, moldBlock);
		return moldBlock;
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

		// Analyze texture to get bounds for custom hitbox
		int[] bounds = analyzeTextureBounds(textureName);

		// Default bounds if analysis failed
		if (bounds == null) {
			bounds = new int[]{0, 0, 15, 15}; // full block minus edge
		}

		// Convert pixel coordinates (0-15) to block coordinates (0-1)
		float minX = Math.max(0, bounds[0] - 1) / 16.0f;
		float minY = 0.0f; // Always start at bottom of block
		float minZ = Math.max(0, bounds[1] - 1) / 16.0f;
		float maxX = Math.min(1, (bounds[2] + 2) / 16.0f);
		float maxY = 0.0625f; // Fixed height for all molds (4 pixels tall)
		float maxZ = Math.min(1, (bounds[3] + 2) / 16.0f);

		// Ensure minimum size
		if (maxX - minX < 0.125f) {
			float center = (minX + maxX) / 2;
			minX = Math.max(0, center - 0.0625f);
			maxX = Math.min(1, center + 0.0625f);
		}
		if (maxZ - minZ < 0.125f) {
			float center = (minZ + maxZ) / 2;
			minZ = Math.max(0, center - 0.0625f);
			maxZ = Math.min(1, center + 0.0625f);
		}

		Forgero.LOGGER.info("Custom model bounds for {}: [{}, {}, {}, {}, {}, {}]",
				textureName, minX, minY, minZ, maxX, maxY, maxZ);

		// SIMPLIFIED APPROACH: Using built-in Minecraft models
		// Base model (empty mold)
		JModel baseModel = JModel.model("minecraft:block/slab");
		baseModel.textures(new net.devtech.arrp.json.models.JTextures()
				.var("bottom", "forgero:block/mold_base")
				.var("top", "forgero:block/molds/" + textureName)
				.var("side", "forgero:block/mold_base")
				.var("particle", "forgero:block/mold_base"));

		// Filled model - need to explicitly show the pattern texture too
		JModel filledModel = JModel.model("minecraft:block/slab");
		filledModel.textures(new net.devtech.arrp.json.models.JTextures()
				.var("bottom", "forgero:block/mold_base")
				.var("top", "forgero:block/molten_metal")  // This will be rendered on top
				.var("side", "forgero:block/mold_base")
				.var("particle", "forgero:block/mold_base"));

		// Progress 33 model
		JModel progress33Model = JModel.model("minecraft:block/slab");
		progress33Model.textures(new net.devtech.arrp.json.models.JTextures()
				.var("bottom", "forgero:block/mold_base")
				.var("top", "forgero:block/molten_metal_33")
				.var("side", "forgero:block/mold_base")
				.var("particle", "forgero:block/mold_base"));

		// Progress 66 model
		JModel progress66Model = JModel.model("minecraft:block/slab");
		progress66Model.textures(new net.devtech.arrp.json.models.JTextures()
				.var("bottom", "forgero:block/mold_base")
				.var("top", "forgero:block/molten_metal_66")
				.var("side", "forgero:block/mold_base")
				.var("particle", "forgero:block/mold_base"));

		// Add models to resource pack
		pack.addModel(baseModel, new Identifier(moldId.getNamespace(), "block/" + moldId.getPath()));
		pack.addModel(filledModel, new Identifier(moldId.getNamespace(), "block/" + moldId.getPath() + "_filled"));
		pack.addModel(progress33Model, new Identifier(moldId.getNamespace(), "block/" + moldId.getPath() + "_progress_33"));
		pack.addModel(progress66Model, new Identifier(moldId.getNamespace(), "block/" + moldId.getPath() + "_progress_66"));

		// Also add special overlay models that show the pattern on top of molten metal
		// This ensures the pattern is visible even when filled
		// Custom model for filled state that shows both pattern and molten metal
		addOverlayModel(pack,
				new Identifier(moldId.getNamespace(), "block/" + moldId.getPath() + "_overlay"),
				"forgero:block/molds/" + textureName,
				"forgero:block/mold_base");

		// Log the model paths
		Forgero.LOGGER.info("Added model at: {}", "forgero:block/" + moldId.getPath());
		Forgero.LOGGER.info("Added model at: {}", "forgero:block/" + moldId.getPath() + "_filled");
		Forgero.LOGGER.info("Added model at: {}", "forgero:block/" + moldId.getPath() + "_progress_33");
		Forgero.LOGGER.info("Added model at: {}", "forgero:block/" + moldId.getPath() + "_progress_66");

		// Create item model that references the block model
		JModel itemModel = JModel.model()
				.parent(moldId.getNamespace() + ":block/" + moldId.getPath());

		pack.addModel(
				itemModel,
				new Identifier(moldId.getNamespace(), "item/" + moldId.getPath())
		);

		// Add to tags
		addTag(pack, new Identifier("minecraft", "mineable/pickaxe"), moldId);
		addTag(pack, new Identifier("minecraft", "needs_stone_tool"), moldId);
		addTag(pack, new Identifier("forgero", "items/molds"), moldId);
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
	 * Add a custom overlay model that combines two textures
	 */
	private void addOverlayModel(RuntimeResourcePack pack, Identifier modelId, String patternTexture, String baseTexture) {
		// Create a custom JSON model that uses overlay textures
		// This is needed because the standard models don't support multiple textures on one face
		String modelJson =
				"{\n" +
						"  \"parent\": \"minecraft:block/block\",\n" +
						"  \"textures\": {\n" +
						"    \"pattern\": \"" + patternTexture + "\",\n" +
						"    \"base\": \"" + baseTexture + "\",\n" +
						"    \"particle\": \"" + baseTexture + "\"\n" +
						"  },\n" +
						"  \"elements\": [\n" +
						"    {\n" +
						"      \"from\": [0, 0, 0],\n" +
						"      \"to\": [16, 4, 16],\n" +
						"      \"faces\": {\n" +
						"        \"down\": {\"uv\": [0, 0, 16, 16], \"texture\": \"#base\", \"cullface\": \"down\"},\n" +
						"        \"up\": {\"uv\": [0, 0, 16, 16], \"texture\": \"#pattern\"},\n" +
						"        \"north\": {\"uv\": [0, 0, 16, 4], \"texture\": \"#base\", \"cullface\": \"north\"},\n" +
						"        \"south\": {\"uv\": [0, 0, 16, 4], \"texture\": \"#base\", \"cullface\": \"south\"},\n" +
						"        \"west\": {\"uv\": [0, 0, 16, 4], \"texture\": \"#base\", \"cullface\": \"west\"},\n" +
						"        \"east\": {\"uv\": [0, 0, 16, 4], \"texture\": \"#base\", \"cullface\": \"east\"}\n" +
						"      }\n" +
						"    }\n" +
						"  ]\n" +
						"}";

		pack.addData(
				new Identifier(modelId.getNamespace(), "models/" + modelId.getPath() + ".json"),
				modelJson.getBytes(java.nio.charset.StandardCharsets.UTF_8)
		);
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
		// Fix: Pass the Identifier directly instead of converting to String
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
				} catch (IOException e) {
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
	 * Add base textures needed for molds
	 */
	private void addBaseTextures(RuntimeResourcePack pack) {
		try {
			// Create a base texture for the mold (stone-like)
			BufferedImage baseTexture = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			// Fill with a stone-like color
			int stoneColor = 0xFF8A8A8A; // Light gray
			for (int x = 0; x < 16; x++) {
				for (int y = 0; y < 16; y++) {
					// Add some noise for a stone-like texture
					int noise = (int) (Math.random() * 20) - 10;
					int r = Math.min(255, Math.max(0, ((stoneColor >> 16) & 0xFF) + noise));
					int g = Math.min(255, Math.max(0, ((stoneColor >> 8) & 0xFF) + noise));
					int b = Math.min(255, Math.max(0, (stoneColor & 0xFF) + noise));
					int color = 0xFF000000 | (r << 16) | (g << 8) | b;
					baseTexture.setRGB(x, y, color);
				}
			}

			// Save and add to resource pack
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(baseTexture, "png", baos);
			Identifier baseTextureId = new Identifier("forgero", "textures/block/mold_base.png");
			pack.addAsset(baseTextureId, baos.toByteArray());

			// Also save for debugging
			ImageIO.write(baseTexture, "png", new File("debug_textures/mold_base.png"));

			// Create molten metal textures (for different cooling stages)
			createMoltenMetalTexture(pack, "molten_metal", 0xFF0000, 0xFF6600);  // Bright orange-red
			createMoltenMetalTexture(pack, "molten_metal_33", 0xDD4400, 0xDD6600);  // Medium orange
			createMoltenMetalTexture(pack, "molten_metal_66", 0xAA5500, 0xAA7700);  // Darker orange-brown

			Forgero.LOGGER.info("Added base textures for molds");
		} catch (Exception e) {
			Forgero.LOGGER.error("Failed to create base textures", e);
		}
	}

	/**
	 * Create a molten metal texture with the given colors
	 */
	private void createMoltenMetalTexture(RuntimeResourcePack pack, String name, int baseColor, int highlightColor) throws IOException {
		BufferedImage texture = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

		// Use a radial gradient and some noise for a realistic molten look
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 16; y++) {
				// Calculate distance from center for gradient
				double distFromCenter = Math.sqrt(Math.pow(x - 7.5, 2) + Math.pow(y - 7.5, 2)) / 11.0;
				distFromCenter = Math.min(1.0, distFromCenter);

				// Add some noise
				double noise = Math.random() * 0.2;

				// Blend between highlight and base color
				double blendFactor = Math.max(0, Math.min(1, distFromCenter + noise));

				int r1 = (highlightColor >> 16) & 0xFF;
				int g1 = (highlightColor >> 8) & 0xFF;
				int b1 = highlightColor & 0xFF;

				int r2 = (baseColor >> 16) & 0xFF;
				int g2 = (baseColor >> 8) & 0xFF;
				int b2 = baseColor & 0xFF;

				int r = (int) (r1 * (1 - blendFactor) + r2 * blendFactor);
				int g = (int) (g1 * (1 - blendFactor) + g2 * blendFactor);
				int b = (int) (b1 * (1 - blendFactor) + b2 * blendFactor);

				int color = 0xFF000000 | (r << 16) | (g << 8) | b;
				texture.setRGB(x, y, color);
			}
		}

		// Save and add to resource pack
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(texture, "png", baos);
		Identifier textureId = new Identifier("forgero", "textures/block/" + name + ".png");
		pack.addAsset(textureId, baos.toByteArray());

		// Also save for debugging
		ImageIO.write(texture, "png", new File("debug_textures/" + name + ".png"));
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

		// First, make sure we have the base textures
		addBaseTextures(pack);

		// Process all textures to create mold versions
		for (String textureName : textureNames) {
			try {
				BufferedImage processedTexture = processTextureForMold(textureName);
				if (processedTexture != null) {
					processedTextures.put(textureName, processedTexture);

					// Ensure we're using the correct image type that preserves transparency
					BufferedImage fixedImage = ensureProperImageType(processedTexture);

					// Debug dump of the texture
					try {
						File debugOutputDir = new File("debug_textures");
						if (!debugOutputDir.exists()) {
							debugOutputDir.mkdirs();
						}
						ImageIO.write(fixedImage, "png", new File(debugOutputDir, textureName + "_mold.png"));
						Forgero.LOGGER.info("Debug texture saved to: {}", new File(debugOutputDir, textureName + "_mold.png").getAbsolutePath());
					} catch (Exception e) {
						Forgero.LOGGER.warn("Failed to save debug texture: {}", e.getMessage());
					}

					// In Minecraft, textures are always under textures/ directory
					ByteArrayOutputStream baos = new ByteArrayOutputStream();

					// Use specialized writer with transparency-preserving settings
					javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
					javax.imageio.ImageWriteParam param = writer.getDefaultWriteParam();

					// Create and configure the output
					javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
					writer.setOutput(ios);

					// Ensure image has transparency information properly set
					ensureAllClearPixelsAreTransparent(fixedImage);

					// Write the image with proper PNG encoding
					javax.imageio.IIOImage iioImage = new javax.imageio.IIOImage(fixedImage, null, null);
					writer.write(null, iioImage, param);

					// Clean up resources
					ios.flush();
					writer.dispose();
					ios.close();

					Identifier textureId = new Identifier("forgero", "textures/block/molds/" + textureName + ".png");
					pack.addAsset(textureId, baos.toByteArray());
					Forgero.LOGGER.info("Added processed mold texture at: {}", textureId);

					// Register the mold block for this texture
					Block moldBlock = registerMoldBlock(textureName);

					// Generate all necessary resources for the mold block
					generateMoldBlockResources(pack, textureName, moldBlock);
				}
			} catch (Exception e) {
				Forgero.LOGGER.error("Failed to process texture for mold: " + textureName, e);
			}
		}

		Forgero.LOGGER.info("Successfully generated resources for {} mold blocks", registeredMoldBlocks.size());

		// Add the registered mold blocks to the smithing creative tab
		addMoldBlocksToSmithingTab();
	}

	/**
	 * Ensures all pixels that should be transparent are fully transparent (alpha=0)
	 * @param image The image to process
	 */
	private void ensureAllClearPixelsAreTransparent(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int pixel = image.getRGB(x, y);
				int alpha = (pixel >> 24) & 0xff;

				// If alpha is very low, make it fully transparent
				if (alpha < 10) {
					image.setRGB(x, y, 0x00000000); // Fully transparent
				}
			}
		}
	}

	/**
	 * Ensures the image is using a format that properly supports transparency
	 * @param original The original image
	 * @return A properly formatted image with transparency support
	 */
	private BufferedImage ensureProperImageType(BufferedImage original) {
		// If already in the correct format, return the original
		if (original.getType() == BufferedImage.TYPE_INT_ARGB) {
			return original;
		}

		// Create a new image with the correct type
		BufferedImage fixed = new BufferedImage(
				original.getWidth(),
				original.getHeight(),
				BufferedImage.TYPE_INT_ARGB);

		// Create a Graphics object for the new image
		java.awt.Graphics2D g2d = fixed.createGraphics();

		// Set background to transparent
		g2d.setColor(new java.awt.Color(0, 0, 0, 0));
		g2d.fillRect(0, 0, fixed.getWidth(), fixed.getHeight());

		// Set composite mode to preserve alpha
		g2d.setComposite(java.awt.AlphaComposite.Src);

		// Draw the original image onto the new one
		g2d.drawImage(original, 0, 0, null);

		// Clean up
		g2d.dispose();

		return fixed;
	}
}
