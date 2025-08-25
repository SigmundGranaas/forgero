// /home/sigmund/Documents/projects/forgero/1-20/forgero-core-2/src/main/java/com/sigmundgranaas/forgero/model/loading/api/item/ModelTemplateProvider.java
package com.sigmundgranaas.forgero.model.loading.api.item;

import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.ArmorModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.EquipmentModelTemplateDTO;
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
	 * @return A map of Part Model Templates, keyed by their shape name for efficient lookup.
	 */
	List<PartModelTemplateDTO> getPartTemplates();

	/**
	 * @return A list of all loaded Upgrade Model Templates.
	 */
	List<UpgradeModelTemplateDTO> getUpgradeTemplates();

	/**
	 * @return A list of all loaded Equipment Model Templates.
	 */
	List<EquipmentModelTemplateDTO> getEquipmentTemplates();

	/**
	 * @return A list of all loaded Armor Model Templates.
	 */
	List<ArmorModelTemplateDTO> getArmorTemplates();
}
