package com.sigmundgranaas.forgero.model.validation.api;

import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.model.validation.impl.DefaultModelValidator;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;

import java.nio.file.Path;
import java.util.List;

/**
 * Validator for Forgero model and texture files.
 * <p>
 * Provides multiple levels of validation:
 * <ul>
 *   <li>Basic model validation (JSON syntax)</li>
 *   <li>Reference validation (textures, parents, slots)</li>
 *   <li>Semantic validation (layers, predicates, mount points)</li>
 *   <li>Palette conformity validation</li>
 *   <li>Animated texture validation</li>
 *   <li>Texture quality validation</li>
 * </ul>
 */
public interface ModelValidator {
	
	/**
	 * Validates model files for basic JSON syntax errors.
	 *
	 * @param provider Resource provider for accessing model files
	 * @param tagResolver Tag resolver for tag-based validation
	 * @return Validation result with errors and warnings
	 */
	ModelValidationResult validateModels(ResourceProvider provider, TagResolver tagResolver);
	
	/**
	 * Validates texture files for format and quality issues.
	 *
	 * @param assetPaths List of asset root paths to scan
	 * @return Validation result with errors and warnings
	 */
	TextureValidationResult validateTextures(List<Path> assetPaths);
	
	/**
	 * Validates palette files for conformity to Forgero's palette specification.
	 *
	 * @param assetPaths List of asset root paths to scan for palettes
	 * @return Validation result with errors and warnings
	 */
	PaletteConformityResult validatePalettes(List<Path> assetPaths);
	
	/**
	 * Validates animated textures and their .mcmeta files.
	 *
	 * @param assetPaths List of asset root paths to scan
	 * @return Validation result with errors and warnings
	 */
	AnimatedTextureResult validateAnimatedTextures(List<Path> assetPaths);
	
	/**
	 * Performs comprehensive validation including all validation types.
	 * <p>
	 * This is the recommended method for full validation as it:
	 * <ul>
	 *   <li>Runs all validation types in optimal order</li>
	 *   <li>Aggregates results into a single comprehensive result</li>
	 *   <li>Provides overall statistics</li>
	 * </ul>
	 *
	 * @param provider Resource provider for accessing files
	 * @param tagResolver Tag resolver for tag-based validation
	 * @param assetPaths List of asset root paths
	 * @return Comprehensive validation result
	 */
	ComprehensiveModelValidationResult validateComprehensive(
			ResourceProvider provider,
			TagResolver tagResolver,
			List<Path> assetPaths
	);
	
	/**
	 * Creates a new ModelValidator with default configuration.
	 */
	static ModelValidator create() {
		return new DefaultModelValidator();
	}
	
	/**
	 * Creates a builder for configuring a ModelValidator.
	 */
	static Builder builder() {
		return new DefaultModelValidator.Builder();
	}
	
	/**
	 * Builder for creating configured ModelValidator instances.
	 */
	interface Builder {
		/**
		 * Sets whether to validate texture references.
		 */
		Builder validateReferences(boolean validate);
		
		/**
		 * Sets whether to perform semantic validation.
		 */
		Builder validateSemantics(boolean validate);
		
		/**
		 * Sets whether to validate palette conformity.
		 */
		Builder validatePalettes(boolean validate);
		
		/**
		 * Sets whether to validate animated textures.
		 */
		Builder validateAnimations(boolean validate);
		
		/**
		 * Builds the configured ModelValidator.
		 */
		ModelValidator build();
	}
}
