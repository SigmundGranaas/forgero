package com.sigmundgranaas.forgero.minecraft.common.item;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningLevel;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningSpeed;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.minecraft.common.conversion.CachedConverter;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class ForgeroMaterial implements ToolMaterial, DynamicAttributeItem {
	private final StateProvider DEFAULT;
	private final Ingredient ingredient;

	public ForgeroMaterial(StateProvider aDefault, Ingredient ingredient) {
		DEFAULT = aDefault;
		this.ingredient = ingredient;
	}

	@Override
	public PropertyContainer dynamicProperties(ItemStack stack) {
		return CachedConverter.of(stack).orElse(DEFAULT.get());
	}

	@Override
	public PropertyContainer defaultProperties() {
		return DEFAULT.get();
	}

	@Override
	public int getDurability() {
		return Durability.apply(defaultProperties());
	}

	@Override
	public float getMiningSpeedMultiplier() {
		return MiningSpeed.apply(defaultProperties());
	}

	@Override
	public float getAttackDamage() {
		return AttackDamage.apply(defaultProperties());
	}

	@Override
	public int getMiningLevel() {
		return MiningLevel.apply(defaultProperties());
	}

	@Override
	public int getEnchantability() {
		return 15;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return ingredient;
	}
}
