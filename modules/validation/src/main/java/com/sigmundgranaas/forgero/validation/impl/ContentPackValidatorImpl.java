package com.sigmundgranaas.forgero.validation.impl;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.common.tags.engine.TagLoadingService;
import com.sigmundgranaas.forgero.core.condition.api.DynamicCondition;
import com.sigmundgranaas.forgero.core.condition.api.StaticCondition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.data.pipeline.api.DataPipelineResult;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
import com.sigmundgranaas.forgero.data.pipeline.api.TemplateExpansionResult;
import com.sigmundgranaas.forgero.model.validation.api.*;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.CompositeResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.FileSystemResourceProvider;
import com.sigmundgranaas.forgero.validation.api.ContentPackValidator;
import com.sigmundgranaas.forgero.validation.api.DefinitionValidationResult;
import com.sigmundgranaas.forgero.validation.api.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main implementation of ContentPackValidator that orchestrates all validation phases.
 * <p>
 * Validation phases:
 * <ol>
 *   <li>Load tags from all content packs</li>
 *   <li>Initialize data pipeline (loads definitions, runs template expansion)</li>
 *   <li>Validate models (if enabled)</li>
 *   <li>Validate textures (if enabled)</li>
 *   <li>Validate palette conformity (if enabled)</li>
 *   <li>Validate animated textures (if enabled)</li>
 *   <li>Aggregate results</li>
 * </ol>
 */
public class ContentPackValidatorImpl implements ContentPackValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContentPackValidatorImpl.class);

	private final List<Path> contentPaths;
	private final String defaultNamespace;
	private final boolean validateModels;
	private final boolean validateTextures;
	private final boolean strictTemplateValidation;
	private final Map<PropertyKey<?>, Codec<? extends List<?>>> propertyCodecs;
	private final Map<String, Codec<? extends StaticCondition>> staticConditionCodecs;
	private final Map<String, Codec<? extends DynamicCondition>> dynamicConditionCodecs;

	ContentPackValidatorImpl(
			List<Path> contentPaths,
			String defaultNamespace,
			boolean validateModels,
			boolean validateTextures,
			boolean strictTemplateValidation,
			Map<PropertyKey<?>, Codec<? extends List<?>>> propertyCodecs,
			Map<String, Codec<? extends StaticCondition>> staticConditionCodecs,
			Map<String, Codec<? extends DynamicCondition>> dynamicConditionCodecs
	) {
		this.contentPaths = List.copyOf(contentPaths);
		this.defaultNamespace = defaultNamespace;
		this.validateModels = validateModels;
		this.validateTextures = validateTextures;
		this.strictTemplateValidation = strictTemplateValidation;
		this.propertyCodecs = Map.copyOf(propertyCodecs);
		this.staticConditionCodecs = Map.copyOf(staticConditionCodecs);
		this.dynamicConditionCodecs = Map.copyOf(dynamicConditionCodecs);
	}

	@Override
	public ValidationResult validate() {
		LOGGER.info("Starting content pack validation...");
		long startTime = System.currentTimeMillis();

		try {
			// Phase 1: Build resource provider from content paths
			ResourceProvider resourceProvider = buildResourceProvider();
			LOGGER.info("Created resource provider from {} content paths", contentPaths.size());

			// Phase 2: Load tags
			TagResolver tagResolver = loadTags(resourceProvider);
			LOGGER.info("Loaded tags from content packs");

			// Phase 3: Initialize data pipeline
			ForgeroDataInitializer.Config config = new ForgeroDataInitializer.Config(
					defaultNamespace,
					resourceProvider,
					tagResolver,
					propertyCodecs,
					staticConditionCodecs,
					dynamicConditionCodecs
			);

			ForgeroDataInitializer initializer = new ForgeroDataInitializer(config);
			DataPipelineResult pipelineResult = initializer.getPipelineResult();

			// Build asset paths for texture validation
			List<Path> assetPaths = buildAssetPaths();
			
			// Create model validator for all texture/model validation
			ModelValidator modelValidator = ModelValidator.create();

			// Phase 4: Model validation (if enabled)
			ModelValidationResult modelResult = null;
			if (validateModels) {
				modelResult = modelValidator.validateModels(resourceProvider, tagResolver);
				LOGGER.info("Validated {} models", modelResult.totalModelsValidated());
			}

			// Phase 5: Texture validation (if enabled)
			TextureValidationResult textureResult = null;
			if (validateTextures) {
				textureResult = modelValidator.validateTextures(assetPaths);
				LOGGER.info("Validated {} templates, {} palettes",
						textureResult.templatesValidated(), textureResult.palettesValidated());
			}

			// Phase 6: Palette conformity validation (if textures enabled)
			PaletteConformityResult paletteResult = null;
			if (validateTextures) {
				paletteResult = modelValidator.validatePalettes(assetPaths);
				LOGGER.info("Validated {} palettes for conformity, {} valid, {} errors, {} warnings",
						paletteResult.totalPalettesValidated(),
						paletteResult.validPalettes(),
						paletteResult.errorCount(),
						paletteResult.warningCount());
			}

			// Phase 7: Animated texture validation (if textures enabled)
			AnimatedTextureResult animatedResult = null;
			if (validateTextures) {
				animatedResult = modelValidator.validateAnimatedTextures(assetPaths);
				LOGGER.info("Validated {} animated textures, {} valid, {} errors",
						animatedResult.totalAnimatedTextures(),
						animatedResult.validAnimatedTextures(),
						animatedResult.errorCount());
			}

			// Phase 8: Build aggregated result
			DefinitionValidationResult definitionResult = new DefinitionValidationResult(
					pipelineResult.parsingErrors(),
					List.of(),
					pipelineResult.rawDefinitions().size()
			);

			ValidationResult.Statistics stats = new ValidationResult.Statistics(
					pipelineResult.rawDefinitions().size(),
					pipelineResult.templateExpansion().templateResultCounts().size(),
					pipelineResult.templateExpansion().totalGenerated(),
					modelResult != null ? modelResult.totalModelsValidated() : 0,
					textureResult != null ? textureResult.templatesValidated() + textureResult.palettesValidated() : 0
			);

			ValidationResult result = new ValidationResult(
					definitionResult,
					pipelineResult.templateExpansion(),
					modelResult,
					textureResult,
					paletteResult,
					animatedResult,
					stats
			);

			long endTime = System.currentTimeMillis();
			LOGGER.info("Validation complete in {}ms. Errors: {}, Warnings: {}",
					endTime - startTime, result.errorCount(), result.warningCount());

			// Log empty templates (CRITICAL ERROR)
			if (result.hasCriticalErrors()) {
				TemplateExpansionResult expansion = result.templateResult();
				LOGGER.error("CRITICAL: {} templates produced 0 results!", expansion.emptyTemplates().size());
				for (OpenIdentifier emptyTemplate : expansion.emptyTemplates()) {
					LOGGER.error("  - {}", emptyTemplate);
				}
			}

			// Log palette errors if any
			if (result.hasPaletteErrors()) {
				LOGGER.error("PALETTE ERRORS: {} palette conformity errors found!", paletteResult.errorCount());
			}

			// Log animated texture errors if any
			if (result.hasAnimatedTextureErrors()) {
				LOGGER.error("ANIMATION ERRORS: {} animated texture errors found!", animatedResult.errorCount());
			}

			return result;

		} catch (Exception e) {
			LOGGER.error("Validation failed with exception", e);
			return ValidationResult.failed(List.of(
					new com.sigmundgranaas.forgero.data.pipeline.api.ParsingError(
							new OpenIdentifier(defaultNamespace, "validation"),
							"Validation failed: " + e.getMessage(),
							e
					)
			));
		}
	}

	/**
	 * Builds a composite resource provider from all content pack paths.
	 */
	private ResourceProvider buildResourceProvider() {
		List<ResourceProvider> providers = new ArrayList<>();

		for (Path contentPath : contentPaths) {
			Path resourcesPath = resolveResourcesPath(contentPath);
			if (Files.exists(resourcesPath)) {
				// Discover namespaces in this content pack
				Set<String> namespaces = discoverNamespaces(resourcesPath.resolve("data"));

				if (!namespaces.isEmpty()) {
					FileSystemResourceProvider provider = new FileSystemResourceProvider(
							resourcesPath,
							"data",
							namespaces,
							50
					);
					providers.add(provider);
					LOGGER.debug("Added provider for {} with namespaces: {}", contentPath, namespaces);
				}
			} else {
				LOGGER.warn("Content path does not exist or has no resources: {}", contentPath);
			}
		}

		if (providers.isEmpty()) {
			throw new IllegalStateException("No valid content paths found. Checked paths: " + contentPaths);
		}

		return CompositeResourceProvider.builder()
				.addAll(providers)
				.withConflictStrategy(CompositeResourceProvider.ConflictStrategy.LAST_WINS)
				.build();
	}

	/**
	 * Resolves the resources path for a content pack.
	 * Content packs are expected to have structure: content-pack/src/main/resources/
	 */
	private Path resolveResourcesPath(Path contentPath) {
		// Try standard gradle structure first
		Path gradlePath = contentPath.resolve("src/main/resources");
		if (Files.exists(gradlePath)) {
			return gradlePath;
		}
		// Fall back to direct path (for non-standard structures)
		return contentPath;
	}

	/**
	 * Discovers all namespaces in a data directory.
	 */
	private Set<String> discoverNamespaces(Path dataPath) {
		if (!Files.exists(dataPath) || !Files.isDirectory(dataPath)) {
			return Set.of();
		}

		try {
			return Files.list(dataPath)
					.filter(Files::isDirectory)
					.map(p -> p.getFileName().toString())
					.collect(Collectors.toSet());
		} catch (Exception e) {
			LOGGER.warn("Failed to discover namespaces in {}", dataPath, e);
			return Set.of();
		}
	}

	/**
	 * Loads tags from all content packs.
	 */
	private TagResolver loadTags(ResourceProvider resourceProvider) {
		IdentifierFactory factory = new IdentifierFactory.Builder()
				.defaultNamespace(defaultNamespace)
				.build();

		TagLoadingService tagService = new TagLoadingService(factory, resourceProvider);

		// Load tags from default namespace
		TagResolver resolver = tagService.loadTags(new OpenIdentifier(defaultNamespace, "tags"));

		// Merge tags from minecraft namespace if present
		if (resourceProvider.getNamespaces().contains("minecraft")) {
			TagResolver minecraftTags = tagService.loadTags(new OpenIdentifier("minecraft", "tags"));
			resolver = resolver.merge(minecraftTags);
		}

		return resolver;
	}

	/**
	 * Builds asset paths for texture validation.
	 */
	private List<Path> buildAssetPaths() {
		List<Path> assetPaths = new ArrayList<>();

		for (Path contentPath : contentPaths) {
			Path resourcesPath = resolveResourcesPath(contentPath);
			// Add the resources path itself (validators will look for assets/ subdirectory)
			assetPaths.add(resourcesPath);
			
			// Also add assets path directly if it exists
			Path assetsPath = resourcesPath.resolve("assets");
			if (Files.exists(assetsPath)) {
				assetPaths.add(assetsPath);
			}
		}

		return assetPaths;
	}
}
