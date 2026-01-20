package com.sigmundgranaas.forgero.tools.item;

import com.sigmundgranaas.forgero.common.item.ForgeroHostItem;
import com.sigmundgranaas.forgero.properties.name.NameResolver;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;

import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;

/**
 * A Forgero-backed pickaxe item.
 * Durability and other stats are provided by the {@link ForgeroToolMaterial} passed to the constructor.
 */
public class ForgeroPickaxeItem extends PickaxeItem implements ForgeroHostItem {
	private final Component component;

	public ForgeroPickaxeItem(ToolMaterial material, Settings settings, Component component) {
		super(material, 0, 0, settings); // Base damage and speed are set to 0, will be provided by attributes
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
