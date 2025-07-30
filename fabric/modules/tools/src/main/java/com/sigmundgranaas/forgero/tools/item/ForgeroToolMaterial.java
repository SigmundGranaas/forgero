package com.sigmundgranaas.forgero.tools.item;

import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;

public class ForgeroToolMaterial implements ToolMaterial {
	private final Component component;
	private final AttributeQueryResult attributes;

	public ForgeroToolMaterial(Component component, Resolver resolver) {
		this.component = component;
		this.attributes = resolver.resolve(component, AttributeEngine.KEY)
				.orElse(AttributeQueryResult.EMPTY);
	}

	@Override
	public int getDurability() {
		return (int) attributes.getValue(DefaultAttributes.DURABILITY);
	}

	@Override
	public float getMiningSpeedMultiplier() {
		return attributes.getValue(DefaultAttributes.MINING_SPEED);
	}

	@Override
	public float getAttackDamage() {
		// Base attack damage for tools is typically handled by the item class,
		// but this can provide a material-based bonus.
		return attributes.getValue(DefaultAttributes.ATTACK_DAMAGE);
	}

	@Override
	public int getMiningLevel() {
		return (int) attributes.getValue(DefaultAttributes.MINING_LEVEL);
	}

	@Override
	public int getEnchantability() {
		return 15;
	}

	@Override
	public Ingredient getRepairIngredient() {
		return Ingredient.EMPTY;
	}
}
