package com.sigmundgranaas.forgero.model.validation.impl;

import com.sigmundgranaas.forgero.model.validation.api.AnimatedTextureResult;
import com.sigmundgranaas.forgero.model.validation.api.AnimatedTextureResult.AnimatedTextureError;
import com.sigmundgranaas.forgero.model.validation.api.AnimatedTextureResult.AnimatedTextureWarning;
import com.sigmundgranaas.forgero.model.validation.api.AnimatedTextureResult.ErrorType;
import com.sigmundgranaas.forgero.model.validation.api.AnimatedTextureResult.WarningType;
import com.sigmundgranaas.forgero.model.validation.util.McmetaParser;
import com.sigmundgranaas.forgero.model.validation.util.McmetaParser.AnimationMeta;
import com.sigmundgranaas.forgero.model.validation.util.McmetaParser.ParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Validates animated textures and their corresponding .mcmeta files.
 * <p>
 * Validation rules:
 * <ul>
 *   <li>Animated textures (height > width for items, height > 1 for palettes) must have .mcmeta</li>
 *   <li>.mcmeta must be valid JSON with "animation" key</li>
 *   <li>frametime must be positive integer</li>
 *   <li>Frame count must match texture dimensions</li>
 *   <li>Custom frame indices must be valid</li>
 * </ul>
 */
public class AnimatedTextureValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(AnimatedTextureValidator.class);
	
	private final McmetaParser mcmetaParser;
	
	/** Maximum frame count before warning about performance */
	private static final int MAX_FRAMES_WARNING = 64;
	
	/** Maximum frametime before warning about slow animation */
	private static final int MAX_FRAMETIME_WARNING = 100;
	
	public AnimatedTextureValidator() {
		this.mcmetaParser = new McmetaParser();
	}
	
	/**
	 * Validates animated textures in the given list of texture paths.
	 *
	 * @param texturePaths List of paths to texture PNG files
	 * @param isPalette If true, uses palette animation detection (height > 1)
	 * @return Validation result
	 */
	public AnimatedTextureResult validate(List<Path> texturePaths, boolean isPalette) {
		List<AnimatedTextureError> errors = new ArrayList<>();
		List<AnimatedTextureWarning> warnings = new ArrayList<>();
		int animatedCount = 0;
		int validCount = 0;
		int orphanedMcmeta = 0;
		
		for (Path texturePath : texturePaths) {
			Path mcmetaPath = Path.of(texturePath.toString() + ".mcmeta");
			boolean mcmetaExists = Files.exists(mcmetaPath);
			
			BufferedImage img;
			try {
				img = ImageIO.read(texturePath.toFile());
				if (img == null) {
					continue; // Skip unreadable files (handled by other validators)
				}
			} catch (IOException e) {
				continue;
			}
			
			boolean looksAnimated = isPalette
					? img.getHeight() > 1
					: img.getHeight() > img.getWidth();
			
			String textureName = extractTextureName(texturePath);
			
			if (looksAnimated && !mcmetaExists) {
				// Animated texture without .mcmeta
				errors.add(new AnimatedTextureError(
						texturePath,
						textureName,
						ErrorType.MISSING_MCMETA,
						String.format("Texture appears animated (dimensions: %dx%d) but no .mcmeta file found",
								img.getWidth(), img.getHeight())
				));
				animatedCount++;
			} else if (!looksAnimated && mcmetaExists) {
				// .mcmeta exists but texture doesn't look animated
				warnings.add(new AnimatedTextureWarning(
						texturePath,
						textureName,
						WarningType.ORPHANED_MCMETA,
						String.format("Texture dimensions (%dx%d) don't suggest animation but .mcmeta exists",
								img.getWidth(), img.getHeight())
				));
				orphanedMcmeta++;
			}
			
			if (mcmetaExists) {
				animatedCount++;
				boolean valid = validateMcmeta(texturePath, mcmetaPath, img, errors, warnings);
				if (valid) {
					validCount++;
				}
			}
		}
		
		return new AnimatedTextureResult(errors, warnings, animatedCount, validCount, orphanedMcmeta);
	}
	
	/**
	 * Discovers and validates all animated textures in the given asset directories.
	 *
	 * @param assetRoots List of asset root directories to scan
	 * @return Validation result
	 */
	public AnimatedTextureResult validateFromAssetRoots(List<Path> assetRoots) {
		List<Path> texturePaths = new ArrayList<>();
		List<Path> palettePaths = new ArrayList<>();
		
		for (Path assetRoot : assetRoots) {
			// Find all textures
			String[] textureLocations = {
					"assets/forgero/textures",
					"forgero/textures"
			};
			
			for (String location : textureLocations) {
				Path textureDir = assetRoot.resolve(location);
				if (Files.exists(textureDir) && Files.isDirectory(textureDir)) {
					try (Stream<Path> files = Files.walk(textureDir)) {
						files.filter(p -> p.toString().toLowerCase().endsWith(".png"))
								.filter(p -> !p.toString().endsWith(".mcmeta"))
								.forEach(p -> {
									// Separate palettes from other textures
									if (p.toString().contains("palette")) {
										palettePaths.add(p);
									} else {
										texturePaths.add(p);
									}
								});
					} catch (IOException e) {
						LOGGER.warn("Failed to scan texture directory: {}", textureDir, e);
					}
				}
			}
		}
		
		// Validate regular textures (animated if height > width)
		AnimatedTextureResult textureResult = validate(texturePaths, false);
		
		// Validate palettes (animated if height > 1)
		AnimatedTextureResult paletteResult = validate(palettePaths, true);
		
		return textureResult.merge(paletteResult);
	}
	
	/**
	 * Validates a .mcmeta file against its texture.
	 *
	 * @return true if valid, false if errors were found
	 */
	private boolean validateMcmeta(
			Path texturePath,
			Path mcmetaPath,
			BufferedImage img,
			List<AnimatedTextureError> errors,
			List<AnimatedTextureWarning> warnings
	) {
		String textureName = extractTextureName(texturePath);
		
		// Parse the .mcmeta file
		ParseResult parseResult = mcmetaParser.parse(mcmetaPath);
		
		if (!parseResult.success()) {
			String errorMessage = parseResult.error().orElse("Unknown parse error");
			
			// Determine specific error type
			ErrorType errorType;
			if (errorMessage.contains("Invalid JSON")) {
				errorType = ErrorType.INVALID_MCMETA_JSON;
			} else if (errorMessage.contains("Missing 'animation'")) {
				errorType = ErrorType.MISSING_ANIMATION_KEY;
			} else if (errorMessage.contains("frametime")) {
				errorType = ErrorType.INVALID_FRAMETIME;
			} else {
				errorType = ErrorType.MCMETA_UNREADABLE;
			}
			
			errors.add(new AnimatedTextureError(
					texturePath,
					mcmetaPath,
					textureName,
					errorType,
					errorMessage
			));
			return false;
		}
		
		AnimationMeta meta = parseResult.meta().orElseThrow();
		
		// Validate against texture dimensions
		Optional<String> dimensionError = mcmetaParser.validateAgainstTexture(
				meta, img.getWidth(), img.getHeight()
		);
		
		if (dimensionError.isPresent()) {
			String errorMsg = dimensionError.get();
			ErrorType errorType = errorMsg.contains("Frame index")
					? ErrorType.INVALID_FRAME_INDEX
					: ErrorType.FRAME_COUNT_MISMATCH;
			
			errors.add(new AnimatedTextureError(
					texturePath,
					mcmetaPath,
					textureName,
					errorType,
					errorMsg
			));
			return false;
		}
		
		// Check for warnings
		int frameCount = mcmetaParser.calculateFrameCount(meta, img.getWidth(), img.getHeight());
		
		// Warn about long animations
		if (frameCount > MAX_FRAMES_WARNING) {
			warnings.add(new AnimatedTextureWarning(
					texturePath,
					textureName,
					WarningType.LONG_ANIMATION,
					String.format("Animation has %d frames, may impact performance", frameCount)
			));
		}
		
		// Warn about slow animations
		if (meta.frametime() > MAX_FRAMETIME_WARNING) {
			warnings.add(new AnimatedTextureWarning(
					texturePath,
					textureName,
					WarningType.SLOW_ANIMATION,
					String.format("frametime is %d ticks (%.1f seconds per frame)",
							meta.frametime(), meta.frametime() / 20.0)
			));
		}
		
		// Warn about interpolation with few frames
		if (meta.interpolate() && frameCount < 4) {
			warnings.add(new AnimatedTextureWarning(
					texturePath,
					textureName,
					WarningType.INTERPOLATION_FEW_FRAMES,
					String.format("Interpolation enabled with only %d frames", frameCount)
			));
		}
		
		return true;
	}
	
	/**
	 * Extracts texture name from path.
	 */
	private String extractTextureName(Path texturePath) {
		String filename = texturePath.getFileName().toString();
		if (filename.toLowerCase().endsWith(".png")) {
			return filename.substring(0, filename.length() - 4);
		}
		return filename;
	}
}
