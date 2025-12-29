package com.sigmundgranaas.forgero.common.api.item.impl;

import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Package-private implementation of {@link ItemQueryApi}.
 * <p>
 * This implementation delegates to existing Forgero services (ComponentConverter, Resolver, SlotManager)
 * and handles null/empty safety for all operations.
 */
public class ItemQueryApiImpl implements ItemQueryApi {

	private final ComponentConverter converter;
	private final Resolver resolver;
	private final SlotManager slotManager;
	private final AttributeEngine attributeEngine;

	/**
	 * Creates a new ItemQueryApiImpl instance.
	 *
	 * @param converter   The component converter
	 * @param resolver    The property resolver
	 * @param slotManager The slot manager
	 */
	public ItemQueryApiImpl(ComponentConverter converter, Resolver resolver, SlotManager slotManager) {
		this.converter = converter;
		this.resolver = resolver;
		this.slotManager = slotManager;
		this.attributeEngine = new AttributeEngine();
	}

	@Override
	public boolean isForgeroItem(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		return converter.toComponent(stack).isPresent();
	}

	@Override
	public boolean isCustomizable(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		return converter.toComponent(stack)
			.map(slotManager::hasComponentUpgradeSlots)
			.orElse(false);
	}

	@Override
	public boolean hasEmptySlots(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		return getEmptySlotCount(stack) > 0;
	}

	@Override
	public boolean isFullyUpgraded(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		return converter.toComponent(stack)
			.map(component -> {
				int totalSlots = slotManager.countComponentUpgradeSlots(component);
				int filledSlots = slotManager.countFilledSlots(component);
				return totalSlots > 0 && totalSlots == filledSlots;
			})
			.orElse(false);
	}

	@Override
	public int getPartCount(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return 0;
		}
		return converter.toComponent(stack)
			.map(component -> {
				List<?> children = component.getChildren();
				// Count structure parts + installed upgrades
				int structureCount = children.size();
				int upgradeCount = slotManager.countFilledSlots(component);
				return structureCount + upgradeCount;
			})
			.orElse(0);
	}

	@Override
	public int getDurability(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return 0;
		}
		// Durability is current damage state in Minecraft, calculated as maxDurability - damage
		int maxDur = getMaxDurability(stack);
		if (maxDur == 0) {
			return 0;
		}
		int damage = stack.getDamage();
		return Math.max(0, maxDur - damage);
	}

	@Override
	public int getMaxDurability(ItemStack stack) {
		return (int) getAttribute(stack, DefaultAttributes.DURABILITY);
	}

	@Override
	public float getAttackDamage(ItemStack stack) {
		return getAttribute(stack, DefaultAttributes.ATTACK_DAMAGE);
	}

	@Override
	public float getMiningSpeed(ItemStack stack) {
		return getAttribute(stack, DefaultAttributes.MINING_SPEED);
	}

	@Override
	public float getAttackSpeed(ItemStack stack) {
		return getAttribute(stack, DefaultAttributes.ATTACK_SPEED);
	}

	@Override
	public int getMiningLevel(ItemStack stack) {
		return (int) getAttribute(stack, DefaultAttributes.MINING_LEVEL);
	}

	@Override
	public int getArmor(ItemStack stack) {
		return (int) getAttribute(stack, DefaultAttributes.ARMOR);
	}

	@Override
	public float getArmorToughness(ItemStack stack) {
		return getAttribute(stack, DefaultAttributes.ARMOR_TOUGHNESS);
	}

	@Override
	public float getAttribute(ItemStack stack, OpenIdentifier attributeType) {
		if (stack == null || stack.isEmpty() || attributeType == null) {
			return 0.0f;
		}
		return converter.toComponent(stack)
			.map(component -> {
				// Resolve attributes using AttributeEngine
				AttributeQueryResult result = resolver.resolve(component, attributeEngine, DynamicContext.empty());
				return result.getValue(attributeType);
			})
			.orElse(0.0f);
	}

	@Override
	public List<ItemStack> getComponents(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return Collections.emptyList();
		}
		return converter.toComponent(stack)
			.map(component -> {
				List<ItemStack> result = new java.util.ArrayList<>();
				// Add structure parts
				result.addAll(getParts(stack));
				// Add installed upgrades
				result.addAll(getInstalledUpgrades(stack));
				return result;
			})
			.orElse(Collections.emptyList());
	}

	@Override
	public List<ItemStack> getParts(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return Collections.emptyList();
		}
		return converter.toComponent(stack)
			.map(component -> {
				// Get children (structure parts)
				return component.getChildren().stream()
					.map(converter::toStack)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(java.util.stream.Collectors.toList());
			})
			.orElse(Collections.emptyList());
	}

	@Override
	public List<ItemStack> getInstalledUpgrades(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return Collections.emptyList();
		}
		return converter.toComponent(stack)
			.map(component -> {
				// Get filled upgrade slots
				return slotManager.getFilledUpgradeSlots(component).stream()
					.map(slot -> slot.getContent().orElse(null))
					.filter(java.util.Objects::nonNull)
					.map(converter::toStack)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(java.util.stream.Collectors.toList());
			})
			.orElse(Collections.emptyList());
	}

	@Override
	public Optional<OpenIdentifier> getPrimaryMaterial(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return Optional.empty();
		}
		return converter.toComponent(stack)
			.flatMap(component -> {
				// Try to find material tag - materials typically have tags like "forgero:iron", "forgero:diamond"
				// Look for tags in the "forgero" namespace that might be materials
				return component.getTags().stream()
					.filter(tag -> tag.namespace().equals("forgero"))
					.filter(tag -> !tag.path().contains("_")) // Materials usually don't have underscores
					.findFirst();
			});
	}

	@Override
	public int getUpgradeSlotCount(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return 0;
		}
		return converter.toComponent(stack)
			.map(slotManager::countComponentUpgradeSlots)
			.orElse(0);
	}

	@Override
	public int getFilledSlotCount(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return 0;
		}
		return converter.toComponent(stack)
			.map(slotManager::countFilledSlots)
			.orElse(0);
	}

	@Override
	public int getEmptySlotCount(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return 0;
		}
		return converter.toComponent(stack)
			.map(slotManager::countEmptySlots)
			.orElse(0);
	}

	@Override
	public boolean hasTag(ItemStack stack, OpenIdentifier tag) {
		if (stack == null || stack.isEmpty() || tag == null) {
			return false;
		}
		return converter.toComponent(stack)
			.map(component -> component.getTags().contains(tag))
			.orElse(false);
	}

	@Override
	public Set<OpenIdentifier> getTags(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return Collections.emptySet();
		}
		return converter.toComponent(stack)
			.map(component -> component.getTags())
			.orElse(Collections.emptySet());
	}
}
