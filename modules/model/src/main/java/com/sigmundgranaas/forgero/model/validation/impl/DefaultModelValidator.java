package com.sigmundgranaas.forgero.model.validation.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.model.validation.api.*;
import com.sigmundgranaas.forgero.model.validation.util.TexturePathResolver;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourcePath;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Default implementation of ModelValidator that performs comprehensive validation.
 */
public class DefaultModelValidator implements ModelValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModelValidator.class);
	
	private final boolean validateReferences;
	private final boolean validateSemantics;
	private final boolean validatePalettesEnabled;
	private final boolean validateAnimations;
	
	public DefaultModelValidator() {
		this(true, true, true, true);
	}
	
	public DefaultModelValidator(
			boolean validateReferences,
			boolean validateSemantics,
			boolean validatePalettes,
			boolean validateAnimations
	) {
		this.validateReferences = validateReferences;
		this.validateSemantics = validateSemantics;
		this.validatePalettesEnabled = validatePalettes;
		this.validateAnimations = validateAnimations;
	}
	
	@Override
	public ModelValidationResult validateModels(ResourceProvider provider, TagResolver tagResolver) {
		List<ModelValidationResult.ModelError> errors = new ArrayList<>();
		List<ModelValidationResult.ModelWarning> warnings = new ArrayList<>();
		int modelCount = 0;
		
		for (String namespace : provider.getNamespaces()) {
			ResourcePath modelsPath = ResourcePath.directory(namespace, "forgero_models");
			
			try (Stream<ResourcePath> modelFiles = provider.list(modelsPath, true)) {
				List<ResourcePath> files = modelFiles.toList();
				for (ResourcePath modelPath : files) {
					modelCount++;
					OpenIdentifier modelId = modelPath.toIdentifier();
					
					try (InputStream is = provider.read(modelPath).orElse(null)) {
						if (is == null) {
							errors.add(new ModelValidationResult.ModelError(
									modelId,
									"Could not read model file",
									ModelValidationResult.ErrorType.INVALID_JSON
							));
							continue;
						}
						
						JsonElement json = JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8));
						if (!json.isJsonObject()) {
							errors.add(new ModelValidationResult.ModelError(
									modelId,
									"Model must be a JSON object",
									ModelValidationResult.ErrorType.INVALID_JSON
							));
						}
					} catch (JsonSyntaxException e) {
						errors.add(new ModelValidationResult.ModelError(
								modelId,
								"Invalid JSON: " + e.getMessage(),
								ModelValidationResult.ErrorType.INVALID_JSON
						));
					} catch (IOException e) {
						errors.add(new ModelValidationResult.ModelError(
								modelId,
								"IO error reading model: " + e.getMessage(),
								ModelValidationResult.ErrorType.INVALID_JSON
						));
					}
				}
			}
		}
		
		return new ModelValidationResult(errors, warnings, modelCount);
	}
	
	@Override
	public TextureValidationResult validateTextures(List<Path> assetPaths) {
		List<TextureValidationResult.TextureError> errors = new ArrayList<>();
		List<TextureValidationResult.TextureWarning> warnings = new ArrayList<>();
		int templateCount = 0;
		int paletteCount = 0;
		
		for (Path assetRoot : assetPaths) {
			// Check multiple possible template locations
			String[] templateLocations = {
					"assets/forgero/templates/textures",
					"assets/forgero/textures/texture_template",
					"forgero/templates/textures",
					"forgero/textures/texture_template"
			};
			
			for (String location : templateLocations) {
				Path templatesDir = assetRoot.resolve(location);
				if (Files.exists(templatesDir)) {
					try (Stream<Path> templates = Files.walk(templatesDir)) {
						List<Path> templateFiles = templates.filter(p -> p.toString().endsWith(".png")).toList();
						for (Path template : templateFiles) {
							templateCount++;
							validateTemplateTexture(template, errors, warnings);
						}
					} catch (IOException e) {
						LOGGER.warn("Failed to scan templates directory: {}", templatesDir, e);
					}
				}
			}
			
			// Check multiple possible palette locations
			String[] paletteLocations = {
					"assets/forgero/templates/materials",
					"assets/forgero/textures/palette",
					"assets/forgero/palettes",
					"forgero/textures/palette",
					"forgero/palettes"
			};
			
			for (String location : paletteLocations) {
				Path palettesDir = assetRoot.resolve(location);
				if (Files.exists(palettesDir)) {
					try (Stream<Path> palettes = Files.walk(palettesDir)) {
						List<Path> paletteFiles = palettes.filter(p -> p.toString().endsWith(".png")).toList();
						for (Path palette : paletteFiles) {
							paletteCount++;
							validatePaletteTextureBasic(palette, errors, warnings);
						}
					} catch (IOException e) {
						LOGGER.warn("Failed to scan palettes directory: {}", palettesDir, e);
					}
				}
			}
		}
		
		return new TextureValidationResult(errors, warnings, templateCount, paletteCount);
	}
	
	@Override
	public PaletteConformityResult validatePalettes(List<Path> assetPaths) {
		PaletteConformityValidator validator = new PaletteConformityValidator();
		return validator.validateFromAssetRoots(assetPaths);
	}
	
	@Override
	public AnimatedTextureResult validateAnimatedTextures(List<Path> assetPaths) {
		AnimatedTextureValidator validator = new AnimatedTextureValidator();
		return validator.validateFromAssetRoots(assetPaths);
	}
	
	@Override
	public ComprehensiveModelValidationResult validateComprehensive(
			ResourceProvider provider,
			TagResolver tagResolver,
			List<Path> assetPaths
	) {
		LOGGER.info("Starting comprehensive model validation...");
		
		ComprehensiveModelValidationResult.Builder builder = ComprehensiveModelValidationResult.builder();
		
		// Phase 1: Basic model validation
		LOGGER.debug("Phase 1: Validating model JSON syntax...");
		ModelValidationResult modelResult = validateModels(provider, tagResolver);
		builder.modelResult(modelResult);
		LOGGER.info("Validated {} models, found {} errors", 
				modelResult.totalModelsValidated(), modelResult.errors().size());
		
		// Phase 2: Texture validation
		LOGGER.debug("Phase 2: Validating textures...");
		TextureValidationResult textureResult = validateTextures(assetPaths);
		builder.textureResult(textureResult);
		LOGGER.info("Validated {} templates, {} palettes", 
				textureResult.templatesValidated(), textureResult.palettesValidated());
		
		// Phase 3: Palette conformity (if enabled)
		if (validatePalettesEnabled) {
			LOGGER.debug("Phase 3: Validating palette conformity...");
			PaletteConformityResult paletteResult = validatePalettes(assetPaths);
			builder.paletteResult(paletteResult);
			LOGGER.info("Validated {} palettes for conformity, {} valid, {} errors",
					paletteResult.totalPalettesValidated(),
					paletteResult.validPalettes(),
					paletteResult.errorCount());
		}
		
		// Phase 4: Animated texture validation (if enabled)
		if (validateAnimations) {
			LOGGER.debug("Phase 4: Validating animated textures...");
			AnimatedTextureResult animatedResult = validateAnimatedTextures(assetPaths);
			builder.animatedResult(animatedResult);
			LOGGER.info("Found {} animated textures, {} valid",
					animatedResult.totalAnimatedTextures(),
					animatedResult.validAnimatedTextures());
		}
		
		// Note: Reference and Semantic validation would require parsed ModelDTOs
		// which requires integrating with the model loading pipeline.
		// For now, we skip these in the default implementation.
		// They can be added when the validator has access to parsed models.
		
		ComprehensiveModelValidationResult result = builder.build();
		
		LOGGER.info("Comprehensive validation complete. Total errors: {}, warnings: {}",
				result.totalErrorCount(), result.totalWarningCount());
		
		return result;
	}
	
	private void validateTemplateTexture(
			Path template,
			List<TextureValidationResult.TextureError> errors,
			List<TextureValidationResult.TextureWarning> warnings
	) {
		try {
			BufferedImage img = ImageIO.read(template.toFile());
			if (img == null) {
				errors.add(new TextureValidationResult.TextureError(
						template,
						"Could not read PNG file",
						TextureValidationResult.ErrorType.INVALID_PNG
				));
				return;
			}
			
			boolean hasNonGrayscale = false;
			for (int y = 0; y < img.getHeight() && !hasNonGrayscale; y++) {
				for (int x = 0; x < img.getWidth() && !hasNonGrayscale; x++) {
					int rgb = img.getRGB(x, y);
					int alpha = (rgb >> 24) & 0xFF;
					if (alpha == 0) continue;
					
					int r = (rgb >> 16) & 0xFF;
					int g = (rgb >> 8) & 0xFF;
					int b = rgb & 0xFF;
					
					if (Math.abs(r - g) > 5 || Math.abs(g - b) > 5 || Math.abs(r - b) > 5) {
						hasNonGrayscale = true;
					}
				}
			}
			
			if (hasNonGrayscale) {
				warnings.add(new TextureValidationResult.TextureWarning(
						template,
						"Template texture contains non-grayscale pixels"
				));
			}
		} catch (IOException e) {
			errors.add(new TextureValidationResult.TextureError(
					template,
					"Failed to read template: " + e.getMessage(),
					TextureValidationResult.ErrorType.INVALID_PNG
			));
		}
	}
	
	private void validatePaletteTextureBasic(
			Path palette,
			List<TextureValidationResult.TextureError> errors,
			List<TextureValidationResult.TextureWarning> warnings
	) {
		try {
			BufferedImage img = ImageIO.read(palette.toFile());
			if (img == null) {
				errors.add(new TextureValidationResult.TextureError(
						palette,
						"Could not read PNG file",
						TextureValidationResult.ErrorType.INVALID_PNG
				));
				return;
			}
			
			// Basic checks (detailed checks in PaletteConformityValidator)
			if (img.getWidth() < 2) {
				errors.add(new TextureValidationResult.TextureError(
						palette,
						"Palette too narrow: " + img.getWidth() + " pixels (minimum 2)",
						TextureValidationResult.ErrorType.PALETTE_TOO_NARROW
				));
			}
		} catch (IOException e) {
			errors.add(new TextureValidationResult.TextureError(
					palette,
					"Failed to read palette: " + e.getMessage(),
					TextureValidationResult.ErrorType.INVALID_PNG
			));
		}
	}
	
	/**
	 * Builder for creating configured DefaultModelValidator instances.
	 */
	public static class Builder implements ModelValidator.Builder {
		private boolean validateReferences = true;
		private boolean validateSemantics = true;
		private boolean validatePalettes = true;
		private boolean validateAnimations = true;
		
		@Override
		public Builder validateReferences(boolean validate) {
			this.validateReferences = validate;
			return this;
		}
		
		@Override
		public Builder validateSemantics(boolean validate) {
			this.validateSemantics = validate;
			return this;
		}
		
		@Override
		public Builder validatePalettes(boolean validate) {
			this.validatePalettes = validate;
			return this;
		}
		
		@Override
		public Builder validateAnimations(boolean validate) {
			this.validateAnimations = validate;
			return this;
		}
		
		@Override
		public ModelValidator build() {
			return new DefaultModelValidator(
					validateReferences,
					validateSemantics,
					validatePalettes,
					validateAnimations
			);
		}
	}
}
