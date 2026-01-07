package com.sigmundgranaas.forgero.model.validation.impl;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.model.loading.impl.dto.*;
import com.sigmundgranaas.forgero.model.validation.api.SemanticValidationResult;
import com.sigmundgranaas.forgero.model.validation.api.SemanticValidationResult.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Validates semantic correctness of model structures.
 * <p>
 * Validates:
 * <ul>
 *   <li>Layer ordering (unique, positive, reasonable gaps)</li>
 *   <li>Predicate configuration (valid types, required fields)</li>
 *   <li>Mount point configuration (unique names)</li>
 *   <li>Slot ID uniqueness</li>
 * </ul>
 */
public class SemanticValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(SemanticValidator.class);
	
	/** Known predicate types and their required fields */
	private static final Map<String, Set<String>> PREDICATE_REQUIREMENTS = Map.of(
			"forgero:root_tag", Set.of("tag"),
			"forgero:child_tag", Set.of("tag"),
			"forgero:bow_pulling", Set.of("pulling"),
			"forgero:bow_pull", Set.of("pull")
	);
	
	/** Maximum reasonable gap between consecutive layer orders */
	private static final int MAX_LAYER_GAP = 50;
	
	/**
	 * Validates semantic correctness of a model.
	 *
	 * @param modelId The model's identifier
	 * @param model The model DTO to validate
	 * @return Validation result
	 */
	public SemanticValidationResult validateModel(OpenIdentifier modelId, ModelDTO model) {
		List<SemanticError> errors = new ArrayList<>();
		List<SemanticWarning> warnings = new ArrayList<>();
		
		// Validate layers
		if (model.layers() != null && !model.layers().isEmpty()) {
			validateLayers(modelId, model.layers(), errors, warnings);
		}
		
		// Validate predicates in textures
		if (model.textures() != null && model.textures().variants() != null) {
			validatePredicates(modelId, model.textures().variants(), "textures.variants", errors, warnings);
		}
		
		// Validate predicates in layer textures
		if (model.layers() != null) {
			for (int i = 0; i < model.layers().size(); i++) {
				LayerDTO layer = model.layers().get(i);
				if (layer.textures() != null && layer.textures().variants() != null) {
					String path = String.format("layers[%d].textures.variants", i);
					validatePredicates(modelId, layer.textures().variants(), path, errors, warnings);
				}
			}
		}
		
		// Validate mount points
		if (model.mountPoints() != null && !model.mountPoints().isEmpty()) {
			validateMountPoints(modelId, model.mountPoints(), errors, warnings);
		}
		
		// Validate slots
		if (model.slots() != null && !model.slots().isEmpty()) {
			validateSlots(modelId, model.slots(), errors, warnings);
		}
		
		return new SemanticValidationResult(errors, warnings, 1);
	}
	
	/**
	 * Validates multiple models.
	 */
	public SemanticValidationResult validateModels(Map<OpenIdentifier, ModelDTO> models) {
		SemanticValidationResult result = SemanticValidationResult.empty();
		
		for (var entry : models.entrySet()) {
			SemanticValidationResult modelResult = validateModel(entry.getKey(), entry.getValue());
			result = result.merge(modelResult);
		}
		
		return result;
	}
	
	/**
	 * Validates layer ordering.
	 */
	private void validateLayers(
			OpenIdentifier modelId,
			List<LayerDTO> layers,
			List<SemanticError> errors,
			List<SemanticWarning> warnings
	) {
		List<Integer> orders = new ArrayList<>();
		Map<Integer, Integer> orderCounts = new HashMap<>();
		
		for (int i = 0; i < layers.size(); i++) {
			int order = layers.get(i).order();
			orders.add(order);
			orderCounts.merge(order, 1, Integer::sum);
			
			// Check for negative order
			if (order < 0) {
				errors.add(new SemanticError(
						modelId,
						ErrorType.NEGATIVE_LAYER_ORDER,
						String.format("layers[%d].order", i),
						"Layer order is negative: " + order
				));
			}
		}
		
		// Check for duplicate orders
		for (var entry : orderCounts.entrySet()) {
			if (entry.getValue() > 1) {
				warnings.add(new SemanticWarning(
						modelId,
						WarningType.DUPLICATE_LAYER_ORDER,
						"layers",
						String.format("Order %d is used by %d layers", entry.getKey(), entry.getValue())
				));
			}
		}
		
		// Check for large gaps
		if (orders.size() > 1) {
			Collections.sort(orders);
			for (int i = 1; i < orders.size(); i++) {
				int gap = orders.get(i) - orders.get(i - 1);
				if (gap > MAX_LAYER_GAP) {
					warnings.add(new SemanticWarning(
							modelId,
							WarningType.LAYER_ORDER_GAP,
							"layers",
							String.format("Large gap (%d) between orders %d and %d",
									gap, orders.get(i - 1), orders.get(i))
					));
				}
			}
		}
	}
	
	/**
	 * Validates predicate configurations.
	 */
	private void validatePredicates(
			OpenIdentifier modelId,
			List<VariantDTO> variants,
			String basePath,
			List<SemanticError> errors,
			List<SemanticWarning> warnings
	) {
		for (int i = 0; i < variants.size(); i++) {
			VariantDTO variant = variants.get(i);
			if (variant.predicate() == null) continue;
			
			for (int j = 0; j < variant.predicate().size(); j++) {
				PredicateDTO predicate = variant.predicate().get(j);
				String path = String.format("%s[%d].predicate[%d]", basePath, i, j);
				
				String type = predicate.type();
				if (type == null || type.isEmpty()) {
					errors.add(new SemanticError(
							modelId,
							ErrorType.MISSING_PREDICATE_FIELD,
							path + ".type",
							"Predicate type is missing"
					));
					continue;
				}
				
				// Check if type is known
				if (!PREDICATE_REQUIREMENTS.containsKey(type)) {
					errors.add(new SemanticError(
							modelId,
							ErrorType.UNKNOWN_PREDICATE_TYPE,
							path + ".type",
							"Unknown predicate type: " + type + ". Known types: " + PREDICATE_REQUIREMENTS.keySet()
					));
					continue;
				}
				
				// Check required fields
				Set<String> required = PREDICATE_REQUIREMENTS.get(type);
				for (String field : required) {
					if (!hasPredicateField(predicate, field)) {
						errors.add(new SemanticError(
								modelId,
								ErrorType.MISSING_PREDICATE_FIELD,
								path + "." + field,
								String.format("Predicate type '%s' requires field '%s'", type, field)
						));
					}
				}
				
				// Validate field values
				validatePredicateValues(modelId, predicate, path, errors);
			}
		}
	}
	
	/**
	 * Checks if a predicate has a specific field set.
	 */
	private boolean hasPredicateField(PredicateDTO predicate, String field) {
		return switch (field) {
			case "tag" -> predicate.tag() != null && !predicate.tag().isEmpty();
			case "pulling" -> predicate.pulling() != null;
			case "pull" -> predicate.pull() != null;
			default -> false;
		};
	}
	
	/**
	 * Validates predicate field values.
	 */
	private void validatePredicateValues(
			OpenIdentifier modelId,
			PredicateDTO predicate,
			String path,
			List<SemanticError> errors
	) {
		// Validate pull value (should be 0.0-1.0)
		if (predicate.pull() != null) {
			float pull = predicate.pull();
			if (pull < 0.0f || pull > 1.0f) {
				errors.add(new SemanticError(
						modelId,
						ErrorType.INVALID_PREDICATE_VALUE,
						path + ".pull",
						String.format("Pull value %.2f is out of range [0.0, 1.0]", pull)
				));
			}
		}
	}
	
	/**
	 * Validates mount point configurations.
	 */
	private void validateMountPoints(
			OpenIdentifier modelId,
			List<MountPointDTO> mountPoints,
			List<SemanticError> errors,
			List<SemanticWarning> warnings
	) {
		Set<String> names = new HashSet<>();
		
		for (int i = 0; i < mountPoints.size(); i++) {
			MountPointDTO mp = mountPoints.get(i);
			String name = mp.name();
			
			if (name == null || name.isEmpty()) {
				errors.add(new SemanticError(
						modelId,
						ErrorType.DUPLICATE_MOUNT_POINT,
						String.format("mountPoints[%d].name", i),
						"Mount point name is missing"
				));
				continue;
			}
			
			if (names.contains(name)) {
				errors.add(new SemanticError(
						modelId,
						ErrorType.DUPLICATE_MOUNT_POINT,
						String.format("mountPoints[%d].name", i),
						"Duplicate mount point name: " + name
				));
			} else {
				names.add(name);
			}
			
			// Check position bounds (warn if potentially outside 16x16)
			if (mp.position() != null && mp.position().size() >= 2) {
				int x = mp.position().get(0);
				int y = mp.position().get(1);
				if (x < 0 || x > 16 || y < 0 || y > 16) {
					warnings.add(new SemanticWarning(
							modelId,
							WarningType.MOUNT_POINT_POSITION,
							String.format("mountPoints[%d].position", i),
							String.format("Position [%d, %d] may be outside standard 16x16 texture bounds", x, y)
					));
				}
			}
		}
	}
	
	/**
	 * Validates slot configurations.
	 */
	private void validateSlots(
			OpenIdentifier modelId,
			List<SlotDTO> slots,
			List<SemanticError> errors,
			List<SemanticWarning> warnings
	) {
		Set<String> ids = new HashSet<>();
		
		for (int i = 0; i < slots.size(); i++) {
			SlotDTO slot = slots.get(i);
			String id = slot.id();
			
			if (id == null || id.isEmpty()) {
				continue; // Handled by other validators
			}
			
			if (ids.contains(id)) {
				warnings.add(new SemanticWarning(
						modelId,
						WarningType.DUPLICATE_SLOT_ID,
						String.format("slots[%d].id", i),
						"Duplicate slot ID: " + id
				));
			} else {
				ids.add(id);
			}
		}
	}
}
