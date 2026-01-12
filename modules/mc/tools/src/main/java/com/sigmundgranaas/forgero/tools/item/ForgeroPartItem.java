package com.sigmundgranaas.forgero.tools.item;

import com.sigmundgranaas.forgero.common.item.ForgeroHostItem;
import com.sigmundgranaas.forgero.common.name.NameResolver;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ForgeroPartItem extends Item implements ForgeroHostItem {
	private final Component component;

	public ForgeroPartItem(Settings settings, Component component) {
		super(settings);
		this.component = component;
	}

	@Override
	public Component getForgeroComponent() {
		return component;
	}

	@Override
	public Text getName() {
		return NameResolver.resolve(component);
	}

	@Override
	public Text getName(ItemStack stack) {
		return ForgeroApi.converter()
				.toComponent(stack)
				.map(NameResolver::resolve)
				.orElseGet(this::getName);
	}
}
