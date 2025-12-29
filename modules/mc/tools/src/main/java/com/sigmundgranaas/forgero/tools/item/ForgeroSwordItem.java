package com.sigmundgranaas.forgero.tools.item;

import com.sigmundgranaas.forgero.common.item.ForgeroHostItem;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;

/**
 * A Forgero-backed sword item.
 * Durability and other stats are provided by the {@link ForgeroToolMaterial} passed to the constructor.
 */
public class ForgeroSwordItem extends SwordItem implements ForgeroHostItem {
	private final Component component;

	public ForgeroSwordItem(ToolMaterial material, Settings settings, Component component, Resolver resolver) {
		super(material, 0, 0, settings);
		this.component = component;
	}

	@Override
	public Component getForgeroComponent() {
		return component;
	}
}
