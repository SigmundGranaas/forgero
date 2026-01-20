package com.sigmundgranaas.forgero.bows.item;

import com.sigmundgranaas.forgero.common.item.ForgeroHostItem;
import com.sigmundgranaas.forgero.properties.name.NameResolver;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * Custom arrow item for Forgero arrows with component-based properties.
 * Extends ArrowItem to maintain vanilla compatibility while adding Forgero functionality.
 */
public class ForgeroArrowItem extends ArrowItem implements ForgeroHostItem {
	private final Component component;

	public ForgeroArrowItem(Settings settings, Component component) {
		super(settings);
		this.component = component;
	}

	@Override
	public Component getForgeroComponent() {
		return component;
	}

	@Override
	public PersistentProjectileEntity createArrow(World world, ItemStack stack, LivingEntity shooter) {
		// Return a vanilla arrow entity - the LaunchProjectileHandler will detect
		// Forgero properties and spawn DynamicArrowEntity instead when appropriate
		return super.createArrow(world, stack, shooter);
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
