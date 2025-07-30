package com.sigmundgranaas.forgero.tools.item;

import com.sigmundgranaas.forgero.common.item.ForgeroHostItem;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;

public class ForgeroPickaxeItem extends PickaxeItem implements ForgeroHostItem {
	private final Component component;

	public ForgeroPickaxeItem(ToolMaterial material, Settings settings, Component component, Resolver resolver) {
		super(material, 0, 0, settings); // Base damage and speed are set to 0, will be provided by attributes
		this.component = component;
	}

	@Override
	public Component getForgeroComponent() {
		return component;
	}

	@Override
	public Component toComponent(ItemStack stack) {
		return ComponentNbtConverter.getInstance().fromNbt(stack.getNbt()).orElse(getForgeroComponent());
	}
}
