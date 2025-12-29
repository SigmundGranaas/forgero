package com.sigmundgranaas.forgero.tools.item;

import com.sigmundgranaas.forgero.common.item.ForgeroHostItem;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ToolMaterial;

/**
 * A Forgero-backed axe item.
 * Durability and other stats are provided by the {@link ForgeroToolMaterial} passed to the constructor.
 */
public class ForgeroAxeItem extends AxeItem implements ForgeroHostItem {
	private final Component component;

	public ForgeroAxeItem(ToolMaterial material, Settings settings, Component component, Resolver resolver) {
		super(material, 0, 0, settings);
		this.component = component;
	}

	@Override
	public Component getForgeroComponent() {
		return component;
	}
}
