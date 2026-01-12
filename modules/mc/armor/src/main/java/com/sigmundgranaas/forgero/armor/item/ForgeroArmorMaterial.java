package com.sigmundgranaas.forgero.armor.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

/**
 * A dynamic, component-driven implementation of ArmorMaterial.
 * Each instance of this class is tied to a specific Forgero armor component
 * and resolves its properties (like protection, durability) from the component's attributes.
 * This class ignores the ArmorItem.Type parameter in its methods, as the backing
 * component already represents a specific armor piece.
 */
public class ForgeroArmorMaterial implements ArmorMaterial {

	private static final Logger LOGGER = LoggerFactory.getLogger(ForgeroArmorMaterial.class);

	private final Component component;
	private final AttributeQueryResult attributes;

	public ForgeroArmorMaterial(Component component) {
		this.component = component;
		this.attributes = new AttributeEngine().resolve(component);
		LOGGER.debug("Created armor material for component: {} with durability={}, armor={}, toughness={}, knockbackResistance={}",
				component.id(),
				(int) attributes.getValue(DefaultAttributes.DURABILITY),
				(int) attributes.getValue(DefaultAttributes.ARMOR),
				attributes.getValue(DefaultAttributes.ARMOR_TOUGHNESS),
				attributes.getValue(DefaultAttributes.KNOCKBACK_RESISTANCE));
	}

	@Override
	public int getDurability(ArmorItem.Type type) {
		return (int) attributes.getValue(DefaultAttributes.DURABILITY);
	}

	@Override
	public int getProtection(ArmorItem.Type type) {
		return (int) attributes.getValue(DefaultAttributes.ARMOR);
	}

	@Override
	public int getEnchantability() {
		return 15;
	}

	@Override
	public SoundEvent getEquipSound() {
		return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
	}

	@Override
	public Ingredient getRepairIngredient() {
		// Repair logic would be more complex, potentially depending on the component's materials.
		return Ingredient.EMPTY;
	}

	@Override
	public String getName() {
		String path = component.id().path();
		int dashIndex = path.indexOf('-');
		if (dashIndex != -1) {
			// This will return "forgero:diamond", which is the correct format.
			return component.id().namespace() + ":" + path.substring(0, dashIndex);
		}
		// Fallback to the original behavior if the format is unexpected.
		return component.id().namespace() + ":" + path;
	}

	@Override
	public float getToughness() {
		return attributes.getValue(DefaultAttributes.ARMOR_TOUGHNESS);
	}

	@Override
	public float getKnockbackResistance() {
		return attributes.getValue(DefaultAttributes.KNOCKBACK_RESISTANCE);
	}
}
