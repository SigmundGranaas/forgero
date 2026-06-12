package com.sigmundgranaas.forgero.model.validation.impl;

import com.sigmundgranaas.forgero.model.validation.api.PaletteConformityResult;
import com.sigmundgranaas.forgero.model.validation.api.PaletteConformityResult.ErrorType;
import com.sigmundgranaas.forgero.model.validation.api.PaletteConformityResult.PaletteError;
import com.sigmundgranaas.forgero.model.validation.api.PaletteConformityResult.PaletteWarning;
import com.sigmundgranaas.forgero.model.validation.api.PaletteConformityResult.WarningType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Validates palette files for conformity to Forgero's palette specification.
 * <p>
 * Strict rules enforced:
 * <ul>
 *   <li>Height must be exactly 1 pixel (ERROR)</li>
 *   <li>Width must be at least 8 pixels (ERROR)</li>
 *   <li>No transparent pixels (ERROR)</li>
 *   <li>Standard widths: 8, 16, or 32 (WARNING)</li>
 *   <li>Consistent width across all palettes (WARNING)</li>
 *   <li>Colors should progress dark to light (WARNING)</li>
 * </ul>
 */
public class PaletteConformityValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(PaletteConformityValidator.class);
	
	/** Required height for non-animated palettes */
	public static final int REQUIRED_HEIGHT = 1;
	
	/** Minimum width for palettes */
	public static final int MINIMUM_WIDTH = 7;
	
	/** Standard/recommended palette widths */
	public static final Set<Integer> STANDARD_WIDTHS = Set.of(7, 8, 16, 32);
	
	/** Luminance difference tolerance for progression check */
	private static final int PROGRESSION_TOLERANCE = 20;
	
	/**
	 * Validates all palette files in the given directories.
	 *
	 * @param palettePaths List of paths to palette PNG files
	 * @return Validation result containing errors and warnings
	 */
	public PaletteConformityResult validate(List<Path> palettePaths) {
		List<PaletteError> errors = new ArrayList<>();
		List<PaletteWarning> warnings = new ArrayList<>();
		Map<Integer, Integer> widthDistribution = new HashMap<>();
		int validCount = 0;
		
		for (Path palettePath : palettePaths) {
			boolean isValid = validateSinglePalette(palettePath, errors, warnings, widthDistribution);
			if (isValid) {
				validCount++;
			}
		}
		
		// Check for inconsistent SHADE COUNT across all palettes.
		// Trailing transparent pixels are allowed padding (e.g. a 7-shade palette padded to 8px),
		// so compare effective (opaque) widths rather than raw pixel widths — otherwise a padded
		// 7-shade palette is falsely reported as inconsistent with an unpadded 7-shade one.
		Map<Integer, Integer> effectiveDistribution = new HashMap<>();
		Map<Path, Integer> effectiveByPath = new HashMap<>();
		for (Path palettePath : palettePaths) {
			try {
				BufferedImage img = ImageIO.read(palettePath.toFile());
				if (img != null && img.getHeight() == REQUIRED_HEIGHT) {
					int eff = effectiveWidth(img);
					effectiveDistribution.merge(eff, 1, Integer::sum);
					effectiveByPath.put(palettePath, eff);
				}
			} catch (IOException ignored) {
				// Already reported as error
			}
		}
		if (effectiveDistribution.size() > 1) {
			int mostCommonWidth = findMostCommonWidth(effectiveDistribution);
			for (Map.Entry<Path, Integer> entry : effectiveByPath.entrySet()) {
				if (entry.getValue() != mostCommonWidth) {
					warnings.add(new PaletteWarning(
							entry.getKey(),
							extractMaterialName(entry.getKey()),
							WarningType.INCONSISTENT_WIDTH,
							String.format("Shade count %d differs from common %d", entry.getValue(), mostCommonWidth)
					));
				}
			}
		}
		
		return new PaletteConformityResult(
				errors,
				warnings,
				palettePaths.size(),
				validCount,
				Map.copyOf(widthDistribution)
		);
	}
	
	/**
	 * Discovers and validates all palettes in the given asset directories.
	 *
	 * @param assetRoots List of asset root directories to scan
	 * @return Validation result
	 */
	public PaletteConformityResult validateFromAssetRoots(List<Path> assetRoots) {
		List<Path> palettePaths = new ArrayList<>();
		
		for (Path assetRoot : assetRoots) {
			// Check common palette locations
			String[] paletteLocations = {
					"assets/forgero/textures/palette",
					"assets/forgero/palettes",
					"forgero/textures/palette",
					"forgero/palettes"
			};
			
			for (String location : paletteLocations) {
				Path paletteDir = assetRoot.resolve(location);
				if (Files.exists(paletteDir) && Files.isDirectory(paletteDir)) {
					try (Stream<Path> files = Files.walk(paletteDir)) {
						files.filter(p -> p.toString().toLowerCase().endsWith(".png"))
								.filter(p -> !p.toString().endsWith(".mcmeta"))
								.forEach(palettePaths::add);
					} catch (IOException e) {
						LOGGER.warn("Failed to scan palette directory: {}", paletteDir, e);
					}
				}
			}
		}
		
		return validate(palettePaths);
	}
	
	/**
	 * Validates a single palette file.
	 *
	 * @return true if the palette is valid (no errors), false otherwise
	 */
	private boolean validateSinglePalette(
			Path palettePath,
			List<PaletteError> errors,
			List<PaletteWarning> warnings,
			Map<Integer, Integer> widthDistribution
	) {
		String materialName = extractMaterialName(palettePath);
		BufferedImage img;
		
		try {
			img = ImageIO.read(palettePath.toFile());
			if (img == null) {
				errors.add(new PaletteError(
						palettePath,
						materialName,
						ErrorType.INVALID_FORMAT,
						"Could not read PNG file"
				));
				return false;
			}
		} catch (IOException e) {
			errors.add(new PaletteError(
					palettePath,
					materialName,
					ErrorType.UNREADABLE,
					"IO error: " + e.getMessage()
			));
			return false;
		}
		
		boolean hasError = false;
		int width = img.getWidth();
		int height = img.getHeight();
		
		// Track width distribution
		widthDistribution.merge(width, 1, Integer::sum);
		
		// Check height (must be 1 for non-animated)
		// Note: animated palettes with height > 1 should have .mcmeta files
		// which are validated separately by AnimatedTextureValidator
		Path mcmetaPath = Path.of(palettePath.toString() + ".mcmeta");
		boolean isAnimated = Files.exists(mcmetaPath);
		
		if (!isAnimated && height != REQUIRED_HEIGHT) {
			errors.add(new PaletteError(
					palettePath,
					materialName,
					ErrorType.INVALID_HEIGHT,
					String.format("Height is %d, must be %d (or provide .mcmeta for animated)", height, REQUIRED_HEIGHT),
					height,
					REQUIRED_HEIGHT
			));
			hasError = true;
		}
		
		// Check minimum width
		if (width < MINIMUM_WIDTH) {
			errors.add(new PaletteError(
					palettePath,
					materialName,
					ErrorType.TOO_NARROW,
					String.format("Width is %d, minimum is %d", width, MINIMUM_WIDTH),
					width,
					MINIMUM_WIDTH
			));
			hasError = true;
		}
		
		// Check for non-standard width (warning only)
		if (width >= MINIMUM_WIDTH && !STANDARD_WIDTHS.contains(width)) {
			warnings.add(new PaletteWarning(
					palettePath,
					materialName,
					WarningType.NON_STANDARD_WIDTH,
					String.format("Width is %d, recommended: 8, 16, or 32", width)
			));
		}
		
		// Check for transparency
		if (hasTransparency(img)) {
			errors.add(new PaletteError(
					palettePath,
					materialName,
					ErrorType.HAS_TRANSPARENCY,
					"Palette contains transparent pixels (alpha < 255)"
			));
			hasError = true;
		}
		
		// Check color progression (for non-animated palettes only)
		if (!isAnimated && height == 1 && hasInvertedProgression(img)) {
			warnings.add(new PaletteWarning(
					palettePath,
					materialName,
					WarningType.INVERTED_PROGRESSION,
					"Colors progress light→dark, expected dark→light (left to right)"
			));
		}
		
		// Check for low contrast
		if (!isAnimated && height == 1 && hasLowContrast(img)) {
			warnings.add(new PaletteWarning(
					palettePath,
					materialName,
					WarningType.LOW_CONTRAST,
					"Palette has very low contrast between first and last colors"
			));
		}
		
		return !hasError;
	}
	
	/**
	 * Checks for invalid transparency (non-trailing transparent pixels).
	 * Trailing transparent pixels are allowed as padding (e.g., 7-color palette padded to 8 pixels).
	 */
	private boolean hasTransparency(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		
		int firstTransparentFromRight = -1;
		for (int x = width - 1; x >= 0; x--) {
			int alpha = (img.getRGB(x, 0) >> 24) & 0xFF;
			if (alpha < 255) {
				firstTransparentFromRight = x;
			} else {
				break;
			}
		}
		
		if (firstTransparentFromRight == -1) {
			return false;
		}
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < firstTransparentFromRight; x++) {
				int alpha = (img.getRGB(x, y) >> 24) & 0xFF;
				if (alpha < 255) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Effective (opaque) width: the number of leading opaque pixels, ignoring trailing transparent
	 * padding. Padding is explicitly allowed (see {@link #hasTransparency}), so this is the real
	 * shade count of the palette.
	 */
	private int effectiveWidth(BufferedImage img) {
		for (int x = img.getWidth() - 1; x >= 0; x--) {
			int alpha = (img.getRGB(x, 0) >> 24) & 0xFF;
			if (alpha == 255) {
				return x + 1;
			}
		}
		return 0;
	}

	/**
	 * Checks if colors progress from light to dark (inverted from expected).
	 * Expected: dark on left, light on right.
	 * <p>
	 * Compares the first opaque pixel against the last <em>opaque</em> pixel so that trailing
	 * transparent padding (luminance 0) does not falsely flag a correct dark→light palette.
	 */
	private boolean hasInvertedProgression(BufferedImage img) {
		int eff = effectiveWidth(img);
		if (eff < 2) return false;

		int firstLuminance = getLuminance(img.getRGB(0, 0));
		int lastLuminance = getLuminance(img.getRGB(eff - 1, 0));

		// Dark should be on left (low luminance), light on right (high luminance)
		return firstLuminance > lastLuminance + PROGRESSION_TOLERANCE;
	}
	
	/**
	 * Checks if the palette has very low contrast.
	 */
	private boolean hasLowContrast(BufferedImage img) {
		int eff = effectiveWidth(img);
		if (eff < 2) return false;

		int minLuminance = Integer.MAX_VALUE;
		int maxLuminance = Integer.MIN_VALUE;

		// Ignore trailing transparent padding (luminance 0 would distort the range).
		for (int x = 0; x < eff; x++) {
			int luminance = getLuminance(img.getRGB(x, 0));
			minLuminance = Math.min(minLuminance, luminance);
			maxLuminance = Math.max(maxLuminance, luminance);
		}
		
		// Consider low contrast if range is less than 50 (out of 255)
		return (maxLuminance - minLuminance) < 50;
	}
	
	/**
	 * Calculates perceived luminance of an RGB color.
	 * Uses standard luminance formula: 0.299*R + 0.587*G + 0.114*B
	 */
	private int getLuminance(int rgb) {
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;
		return (int) (0.299 * r + 0.587 * g + 0.114 * b);
	}
	
	/**
	 * Finds the most common width from the distribution map.
	 */
	private int findMostCommonWidth(Map<Integer, Integer> widthDistribution) {
		return widthDistribution.entrySet().stream()
				.max(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.orElse(16);
	}
	
	/**
	 * Extracts the material name from a palette file path.
	 * Example: /path/to/palette/iron.png → iron
	 */
	private String extractMaterialName(Path palettePath) {
		String filename = palettePath.getFileName().toString();
		if (filename.toLowerCase().endsWith(".png")) {
			return filename.substring(0, filename.length() - 4);
		}
		return filename;
	}
}
