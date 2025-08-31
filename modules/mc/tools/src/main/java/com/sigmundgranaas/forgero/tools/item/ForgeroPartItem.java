package com.sigmundgranaas.forgero.tools.item;

import com.sigmundgranaas.forgero.common.item.ForgeroHostItem;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;

import net.minecraft.item.Item;

public class ForgeroPartItem extends Item implements ForgeroHostItem {
	private final Component component;

	public ForgeroPartItem(Settings settings, Component component, Resolver resolver) {
		super(settings);
		this.component = component;
	}

	@Override
	public Component getForgeroComponent() {
		return component;
	}

}
