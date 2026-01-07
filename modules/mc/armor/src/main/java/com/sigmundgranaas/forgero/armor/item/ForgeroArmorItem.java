package com.sigmundgranaas.forgero.armor.item;

import com.sigmundgranaas.forgero.common.item.ForgeroHostItem;
import com.sigmundgranaas.forgero.common.name.NameResolver;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * A Forgero-specific armor item. It extends the vanilla ArmorItem but is also a
 * ForgeroHostItem, holding a direct reference to its default Forgero component.
 * This allows the item to be a bridge between the vanilla item system and Forgero's component system.
 */
public class ForgeroArmorItem extends ArmorItem implements ForgeroHostItem {
	private final Component component;

	public ForgeroArmorItem(ArmorMaterial material, Type type, Settings settings, Component component) {
		super(material, type, settings);
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
