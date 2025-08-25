package com.sigmundgranaas.forgero.model.loading.api.item;

import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.ArmorModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.PartModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.UpgradeModelTemplateDTO;

import java.util.List;

/**
 * Provides access to loaded model template DTOs for all model types.
 * This service is responsible for loading all template files and making them
 * available to the model generation pipeline.
 */
public interface ModelTemplateProvider {
	/**
	 * @return A list of all loaded Part and Equipment Model Templates.
	 */
	List<PartModelTemplateDTO> getItemTemplates();

	/**
	 * @return A list of all loaded Upgrade Model Templates.
	 */
	List<UpgradeModelTemplateDTO> getUpgradeTemplates();

	/**
	 * @return A list of all loaded Armor Model Templates.
	 */
	List<ArmorModelTemplateDTO> getArmorTemplates();
}
