package com.sigmundgranaas.forgero.model.validation.api;

/**
 * Comprehensive result aggregating all model and texture validation results.
 * <p>
 * Includes:
 * <ul>
 *   <li>Basic model validation (JSON syntax)</li>
 *   <li>Reference validation (textures, parents, slots)</li>
 *   <li>Semantic validation (layers, predicates, mount points)</li>
 *   <li>Palette conformity validation</li>
 *   <li>Animated texture validation</li>
 *   <li>Basic texture validation</li>
 * </ul>
 */
public record ComprehensiveModelValidationResult(
		ModelValidationResult modelResult,
		ReferenceValidationResult referenceResult,
		SemanticValidationResult semanticResult,
		PaletteConformityResult paletteResult,
		AnimatedTextureResult animatedResult,
		TextureValidationResult textureResult,
		Statistics statistics
) {
	
	/**
	 * Overall statistics for the validation run.
	 */
	public record Statistics(
			int totalModelsScanned,
			int totalTexturesScanned,
			int totalPalettesScanned,
			int totalReferencesChecked,
			int totalAnimatedTextures
	) {}
	
	/**
	 * Returns true if any validation found errors.
	 */
	public boolean hasErrors() {
		return (modelResult != null && modelResult.hasErrors())
				|| (referenceResult != null && referenceResult.hasErrors())
				|| (semanticResult != null && semanticResult.hasErrors())
				|| (paletteResult != null && paletteResult.hasErrors())
				|| (animatedResult != null && animatedResult.hasErrors())
				|| (textureResult != null && textureResult.hasErrors());
	}
	
	/**
	 * Returns total error count across all validations.
	 */
	public int totalErrorCount() {
		int count = 0;
		if (modelResult != null) count += modelResult.errors().size();
		if (referenceResult != null) count += referenceResult.errorCount();
		if (semanticResult != null) count += semanticResult.errorCount();
		if (paletteResult != null) count += paletteResult.errorCount();
		if (animatedResult != null) count += animatedResult.errorCount();
		if (textureResult != null) count += textureResult.errors().size();
		return count;
	}
	
	/**
	 * Returns total warning count across all validations.
	 */
	public int totalWarningCount() {
		int count = 0;
		if (modelResult != null) count += modelResult.warnings().size();
		if (referenceResult != null) count += referenceResult.warningCount();
		if (semanticResult != null) count += semanticResult.warningCount();
		if (paletteResult != null) count += paletteResult.warningCount();
		if (animatedResult != null) count += animatedResult.warningCount();
		if (textureResult != null) count += textureResult.warnings().size();
		return count;
	}
	
	/**
	 * Creates an empty result with no validations performed.
	 */
	public static ComprehensiveModelValidationResult empty() {
		return new ComprehensiveModelValidationResult(
				ModelValidationResult.empty(),
				ReferenceValidationResult.empty(),
				SemanticValidationResult.empty(),
				PaletteConformityResult.empty(),
				AnimatedTextureResult.empty(),
				TextureValidationResult.empty(),
				new Statistics(0, 0, 0, 0, 0)
		);
	}
	
	/**
	 * Builder for creating ComprehensiveModelValidationResult.
	 */
	public static class Builder {
		private ModelValidationResult modelResult = ModelValidationResult.empty();
		private ReferenceValidationResult referenceResult = ReferenceValidationResult.empty();
		private SemanticValidationResult semanticResult = SemanticValidationResult.empty();
		private PaletteConformityResult paletteResult = PaletteConformityResult.empty();
		private AnimatedTextureResult animatedResult = AnimatedTextureResult.empty();
		private TextureValidationResult textureResult = TextureValidationResult.empty();
		
		public Builder modelResult(ModelValidationResult result) {
			this.modelResult = result != null ? result : ModelValidationResult.empty();
			return this;
		}
		
		public Builder referenceResult(ReferenceValidationResult result) {
			this.referenceResult = result != null ? result : ReferenceValidationResult.empty();
			return this;
		}
		
		public Builder semanticResult(SemanticValidationResult result) {
			this.semanticResult = result != null ? result : SemanticValidationResult.empty();
			return this;
		}
		
		public Builder paletteResult(PaletteConformityResult result) {
			this.paletteResult = result != null ? result : PaletteConformityResult.empty();
			return this;
		}
		
		public Builder animatedResult(AnimatedTextureResult result) {
			this.animatedResult = result != null ? result : AnimatedTextureResult.empty();
			return this;
		}
		
		public Builder textureResult(TextureValidationResult result) {
			this.textureResult = result != null ? result : TextureValidationResult.empty();
			return this;
		}
		
		public ComprehensiveModelValidationResult build() {
			Statistics stats = new Statistics(
					modelResult.totalModelsValidated(),
					textureResult.templatesValidated() + textureResult.palettesValidated(),
					paletteResult.totalPalettesValidated(),
					referenceResult.totalReferencesChecked(),
					animatedResult.totalAnimatedTextures()
			);
			
			return new ComprehensiveModelValidationResult(
					modelResult,
					referenceResult,
					semanticResult,
					paletteResult,
					animatedResult,
					textureResult,
					stats
			);
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
}
