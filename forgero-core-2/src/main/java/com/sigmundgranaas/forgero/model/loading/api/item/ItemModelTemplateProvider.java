package com.sigmundgranaas.forgero.model.loading.api.item;

import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.ContextualModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.EquipmentModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.PartModelTemplateDTO;

import java.util.List;
import java.util.Map;

/**
 * Provides access to loaded model template DTOs.
 * This service is responsible for loading all template files and making them
 * available to the model generation pipeline.
 */
public interface ItemModelTemplateProvider {
	/**
	 * @return A map of Part Model Templates, keyed by their shape name for efficient lookup.
	 * The shape name is derived from the template's filename (e.g., "sword_blade.json" -> "sword_blade").
	 */
	Map<String, PartModelTemplateDTO> getPartTemplates();

	/**
	 * @return A list of all loaded Contextual Model Templates.
	 */
	List<ContextualModelTemplateDTO> getContextualTemplates();

	/**
	 * @return A list of all loaded Equipment Model Templates.
	 */
	List<EquipmentModelTemplateDTO> getEquipmentTemplates();
}
