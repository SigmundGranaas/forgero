package com.sigmundgranaas.forgero.minecraft.common.item;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningLevel;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.MiningSpeed;
import com.sigmundgranaas.forgero.core.state.StateProvider;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class ForgeroMaterial implements ToolMaterial, DynamicAttributeItem {
	private final StateProvider DEFAULT;
	private final Ingredient ingredient;
	private final StateService service;

	public ForgeroMaterial(StateProvider aDefault, Ingredient ingredient, StateService service) {
		DEFAULT = aDefault;
		this.ingredient = ingredient;
		this.service = service;
	}

	@Override
	public PropertyContainer dynamicProperties(ItemStack stack) {
		return service.convert(stack).orElse(DEFAULT.get());
	}

	@Override
	public PropertyContainer defaultProperties() {
		return DEFAULT.get();
	}

	@Override
	public int getDurability() {
		return ComputedAttribute.of(defaultProperties(), Durability.KEY).asInt();
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
		return ComputedAttribute.of(defaultProperties(), MiningLevel.KEY).asInt();
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
