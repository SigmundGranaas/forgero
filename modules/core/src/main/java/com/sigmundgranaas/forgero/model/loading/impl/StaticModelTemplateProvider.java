package com.sigmundgranaas.forgero.model.loading.impl;

import com.sigmundgranaas.forgero.model.loading.api.item.ModelTemplateProvider;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.ArmorModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.PartModelTemplateDTO;
import com.sigmundgranaas.forgero.model.loading.impl.dto.templates.UpgradeModelTemplateDTO;

import java.util.List;

public class StaticModelTemplateProvider implements ModelTemplateProvider {

	private final List<PartModelTemplateDTO> itemTemplates;
	private final List<UpgradeModelTemplateDTO> upgradeTemplates;
	private final List<ArmorModelTemplateDTO> armorTemplates;

	public StaticModelTemplateProvider(List<PartModelTemplateDTO> itemTemplates, List<UpgradeModelTemplateDTO> upgradeTemplates, List<ArmorModelTemplateDTO> armorTemplates) {
		this.itemTemplates = itemTemplates;
		this.upgradeTemplates = upgradeTemplates;
		this.armorTemplates = armorTemplates;
	}

	@Override
	public List<PartModelTemplateDTO> getItemTemplates() {
		return itemTemplates;
	}

	@Override
	public List<UpgradeModelTemplateDTO> getUpgradeTemplates() {
		return upgradeTemplates;
	}

	@Override
	public List<ArmorModelTemplateDTO> getArmorTemplates() {
		return armorTemplates;
	}
}
