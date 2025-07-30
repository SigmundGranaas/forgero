package com.sigmundgranaas.forgero.common.service;

import com.sigmundgranaas.forgero.common.item.ForgeroHostItem;
import com.sigmundgranaas.forgero.common.item.ItemToComponentMapper;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public class ComponentService {
	public static final ComponentService INSTANCE = new ComponentService();
	private ComponentRegistry componentRegistry;

	private ComponentService() {
	}

	public void initialize(ComponentRegistry componentRegistry) {
		this.componentRegistry = componentRegistry;
	}

	private ComponentRegistry getComponentRegistry() {
		if (componentRegistry == null) {
			throw new IllegalStateException("ComponentService has not been initialized with a ComponentRegistry.");
		}
		return componentRegistry;
	}

	public Optional<Component> getComponent(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return Optional.empty();
		}

		// Priority 1: The item is a ForgeroHostItem. It knows how to convert itself.
		if (stack.getItem() instanceof ForgeroHostItem host) {
			return Optional.of(host.toComponent(stack));
		}

		// Priority 2: The item is mapped in the ItemToComponentMapper (e.g., vanilla items).
		return ItemToComponentMapper.getInstance()
				.getComponentId(stack)
				.flatMap(id -> getComponentRegistry().get(id));
	}
}
