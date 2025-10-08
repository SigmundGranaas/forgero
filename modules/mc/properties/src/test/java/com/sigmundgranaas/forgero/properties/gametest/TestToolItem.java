package com.sigmundgranaas.forgero.properties.gametest;

import com.sigmundgranaas.forgero.common.item.ForgeroHostItem;
import com.sigmundgranaas.forgero.core.component.api.Component;
import net.minecraft.item.Item;

public class TestToolItem extends Item implements ForgeroHostItem {
	private final Component component;

	public TestToolItem(Settings settings, Component component) {
		super(settings);
		this.component = component;
	}

	@Override
	public Component getForgeroComponent() {
		return component;
	}
}
