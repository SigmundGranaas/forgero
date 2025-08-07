package com.sigmundgranaas.forgero.common.convert;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class ComponentConverterImpl implements ComponentConverter {
	private final StatefulConverter statefulConverter;
	private final TypeConverter typeConverter;
	private final IdMapper idMapper;
	private final ComponentRegistry componentRegistry;

	public ComponentConverterImpl(StatefulConverter statefulConverter, TypeConverter typeConverter, IdMapper idMapper, ComponentRegistry componentRegistry) {
		this.statefulConverter = statefulConverter;
		this.typeConverter = typeConverter;
		this.idMapper = idMapper;
		this.componentRegistry = componentRegistry;
	}

	@Override
	public Optional<Component> toComponent(ItemStack stack) {
		return statefulConverter.toComponent(stack);
	}

	@Override
	public Optional<Component> toComponent(Item item) {
		return typeConverter.toComponent(item);
	}

	@Override
	public Optional<Component> toComponent(Identifier itemId) {
		return typeConverter.toComponent(itemId);
	}

	@Override
	public Optional<ItemStack> toStack(Component component) {
		return statefulConverter.toStack(component);
	}

	@Override
	public Optional<ItemStack> toStack(OpenIdentifier componentId) {
		return componentRegistry.get(componentId).flatMap(this::toStack);
	}

	@Override
	public Optional<Item> toItem(Component component) {
		return typeConverter.toItem(component);
	}

	@Override
	public Optional<Item> toItem(OpenIdentifier componentId) {
		return typeConverter.toItem(componentId);
	}

	@Override
	public Optional<Identifier> toItemId(OpenIdentifier componentId) {
		return idMapper.toItemId(componentId);
	}

	@Override
	public Optional<OpenIdentifier> toComponentId(ItemStack stack) {
		return idMapper.toComponentId(stack);
	}

	@Override
	public Optional<OpenIdentifier> toComponentId(Item item) {
		return idMapper.toComponentId(new ItemStack(item));
	}
}
