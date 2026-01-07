package com.sigmundgranaas.forgero.model.validation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.loading.impl.dto.*;
import com.sigmundgranaas.forgero.model.validation.api.ReferenceValidationResult;
import com.sigmundgranaas.forgero.model.validation.api.ReferenceValidationResult.*;
import com.sigmundgranaas.forgero.model.validation.util.TexturePathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Validates references within model files.
 * <p>
 * Validates:
 * <ul>
 *   <li>Texture references - do referenced textures exist?</li>
 *   <li>Parent model references - do parent models exist?</li>
 *   <li>Slot references - are renderer types valid?</li>
 * </ul>
 */
public class ReferenceValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceValidator.class);
	
	/** Known valid renderer types */
	private static final Set<String> VALID_RENDERER_TYPES = Set.of(
			"forgero:component",
			"forgero:default"
	);
	
	private final TexturePathResolver textureResolver;
	private final Set<OpenIdentifier> knownModels;
	
	/**
	 * Creates a reference validator.
	 *
	 * @param textureResolver Resolver for checking texture existence
	 * @param knownModels Set of known model identifiers for parent reference validation
	 */
	public ReferenceValidator(TexturePathResolver textureResolver, Set<OpenIdentifier> knownModels) {
		this.textureResolver = textureResolver;
		this.knownModels = Set.copyOf(knownModels);
	}
	
	/**
	 * Validates all references in a model.
	 *
	 * @param modelId The model's identifier
	 * @param model The model DTO to validate
	 * @return Validation result
	 */
	public ReferenceValidationResult validateModel(OpenIdentifier modelId, ModelDTO model) {
		List<TextureReferenceError> textureErrors = new ArrayList<>();
		List<ParentReferenceError> parentErrors = new ArrayList<>();
		List<SlotReferenceError> slotErrors = new ArrayList<>();
		List<ReferenceWarning> warnings = new ArrayList<>();
		int referencesChecked = 0;
		
		// Validate parent reference
		if (model.parent() != null && !model.parent().isEmpty()) {
			referencesChecked++;
			validateParentReference(modelId, model.parent(), parentErrors, warnings);
		}
		
		// Validate single texture reference
		if (model.texture() != null && !model.texture().isEmpty()) {
			referencesChecked++;
			validateTextureReference(modelId, model.texture(), "texture", textureErrors);
		}
		
		// Validate textures in TexturesDTO
		if (model.textures() != null) {
			referencesChecked += validateTexturesDTO(modelId, model.textures(), "textures", textureErrors);
		}
		
		// Validate layers
		if (model.layers() != null) {
			for (int i = 0; i < model.layers().size(); i++) {
				LayerDTO layer = model.layers().get(i);
				if (layer.textures() != null) {
					String path = String.format("layers[%d].textures", i);
					referencesChecked += validateTexturesDTO(modelId, layer.textures(), path, textureErrors);
				}
			}
		}
		
		// Validate slots
		if (model.slots() != null) {
			for (SlotDTO slot : model.slots()) {
				referencesChecked++;
				validateSlot(modelId, slot, slotErrors, warnings);
			}
		}
		
		return new ReferenceValidationResult(
				textureErrors,
				parentErrors,
				slotErrors,
				warnings,
				referencesChecked
		);
	}
	
	/**
	 * Validates references in multiple models.
	 *
	 * @param models Map of model ID to model DTO
	 * @return Aggregated validation result
	 */
	public ReferenceValidationResult validateModels(Map<OpenIdentifier, ModelDTO> models) {
		ReferenceValidationResult result = ReferenceValidationResult.empty();
		
		for (var entry : models.entrySet()) {
			ReferenceValidationResult modelResult = validateModel(entry.getKey(), entry.getValue());
			result = result.merge(modelResult);
		}
		
		return result;
	}
	
	/**
	 * Validates a parent model reference.
	 */
	private void validateParentReference(
			OpenIdentifier modelId,
			String parentRef,
			List<ParentReferenceError> errors,
			List<ReferenceWarning> warnings
	) {
		// Skip placeholder references (they'll be resolved at runtime)
		if (parentRef.contains("{")) {
			warnings.add(new ReferenceWarning(
					modelId,
					"parent",
					"Parent reference contains placeholder: " + parentRef
			));
			return;
		}
		
		OpenIdentifier parentId = OpenIdentifier.parse(parentRef);
		
		if (!knownModels.contains(parentId)) {
			errors.add(new ParentReferenceError(
					modelId,
					parentRef,
					"forgero_models/" + parentId.path() + ".json"
			));
		}
	}
	
	/**
	 * Validates a texture reference.
	 */
	private void validateTextureReference(
			OpenIdentifier modelId,
			String textureRef,
			String fieldPath,
			List<TextureReferenceError> errors
	) {
		// Skip placeholder references
		if (textureRef.contains("{")) {
			return;
		}
		
		// Skip empty references
		if (textureRef.isEmpty()) {
			return;
		}
		
		if (!textureResolver.textureExists(textureRef)) {
			errors.add(new TextureReferenceError(
					modelId,
					textureRef,
					fieldPath,
					textureResolver.getExpectedTexturePath(textureRef)
			));
		}
	}
	
	/**
	 * Validates a TexturesDTO (default texture and variants).
	 *
	 * @return Number of references checked
	 */
	private int validateTexturesDTO(
			OpenIdentifier modelId,
			TexturesDTO textures,
			String basePath,
			List<TextureReferenceError> errors
	) {
		int count = 0;
		
		// Validate default texture
		if (textures.defaultTexture() != null && !textures.defaultTexture().isEmpty()) {
			count++;
			validateTextureReference(modelId, textures.defaultTexture(), basePath + ".default", errors);
		}
		
		// Validate variants
		if (textures.variants() != null) {
			for (int i = 0; i < textures.variants().size(); i++) {
				VariantDTO variant = textures.variants().get(i);
				if (variant.texture() != null && !variant.texture().isEmpty()) {
					count++;
					String path = String.format("%s.variants[%d].texture", basePath, i);
					validateTextureReference(modelId, variant.texture(), path, errors);
				}
			}
		}
		
		return count;
	}
	
	/**
	 * Validates a slot configuration.
	 */
	private void validateSlot(
			OpenIdentifier modelId,
			SlotDTO slot,
			List<SlotReferenceError> errors,
			List<ReferenceWarning> warnings
	) {
		if (slot.renderer() == null) {
			return;
		}
		
		String rendererType = slot.renderer().type();
		
		if (rendererType == null || rendererType.isEmpty()) {
			errors.add(new SlotReferenceError(
					modelId,
					slot.id(),
					"<empty>",
					"Slot renderer type is missing"
			));
			return;
		}
		
		if (!VALID_RENDERER_TYPES.contains(rendererType)) {
			errors.add(new SlotReferenceError(
					modelId,
					slot.id(),
					rendererType,
					"Unknown renderer type. Valid types: " + VALID_RENDERER_TYPES
			));
		}
		
		// Check for context when using component renderer
		if ("forgero:component".equals(rendererType) && slot.renderer().context() != null) {
			warnings.add(new ReferenceWarning(
					modelId,
					"slots." + slot.id() + ".renderer.context",
					"Slot uses contextual rendering with context: " + slot.renderer().context()
			));
		}
	}
	
	/**
	 * Builder for creating ReferenceValidator with collected model IDs.
	 */
	public static class Builder {
		private TexturePathResolver textureResolver;
		private final Set<OpenIdentifier> knownModels = new HashSet<>();
		
		public Builder textureResolver(TexturePathResolver resolver) {
			this.textureResolver = resolver;
			return this;
		}
		
		public Builder addModel(OpenIdentifier modelId) {
			knownModels.add(modelId);
			return this;
		}
		
		public Builder addModels(Collection<OpenIdentifier> modelIds) {
			knownModels.addAll(modelIds);
			return this;
		}
		
		public ReferenceValidator build() {
			Objects.requireNonNull(textureResolver, "TexturePathResolver is required");
			return new ReferenceValidator(textureResolver, knownModels);
		}
	}
	
	public static Builder builder() {
		return new Builder();
	}
}
