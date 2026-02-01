package com.sigmundgranaas.forgero.common.api.item.impl;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.sigmundgranaas.forgero.common.api.item.ItemQueryApi;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.EquipmentComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.SlotManager;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * Package-private implementation of {@link ItemQueryApi}.
 * <p>
 * This implementation delegates to existing Forgero services (ComponentConverter, Resolver, SlotManager)
 * and handles null/empty safety for all operations.
 */
public class ItemQueryApiImpl implements ItemQueryApi {

	private final ComponentConverter converter;
	private final SlotManager slotManager;
	private final AttributeEngine attributeEngine;

	/**
	 * Creates a new ItemQueryApiImpl instance.
	 *
	 * @param converter   The component converter
	 * @param slotManager The slot manager
	 */
	public ItemQueryApiImpl(ComponentConverter converter, SlotManager slotManager) {
		this.converter = converter;
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
	public float getMiningSpeed(ItemStack stack, net.minecraft.block.BlockState state) {
		if (stack == null || stack.isEmpty() || state == null) {
			return 0.0f;
		}

		// Check if the tool is effective on this block using vanilla logic
		boolean isEffective = stack.isSuitableFor(state);

		if (!isEffective) {
			return 1.0f; // Return base speed (1.0) when not effective
		}

		// Return Forgero's mining speed when effective
		return getMiningSpeed(stack);
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
				AttributeQueryResult result = attributeEngine.resolve(component, DynamicContext.empty());
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
	public boolean appliesAttributes(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		return converter.toComponent(stack)
			.map(component -> component instanceof EquipmentComponent)
			.orElse(false);
	}

	@Override
	public float getContributedAttackDamage(ItemStack stack) {
		// This method returns the attack damage regardless of component type
		// It's the same as getAttribute, which works for both equipment and parts
		return getAttribute(stack, DefaultAttributes.ATTACK_DAMAGE);
	}

	@Override
	public float getContributedMiningSpeed(ItemStack stack) {
		return getAttribute(stack, DefaultAttributes.MINING_SPEED);
	}

	@Override
	public int getContributedDurability(ItemStack stack) {
		return (int) getAttribute(stack, DefaultAttributes.DURABILITY);
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

	// Vanilla attribute modifier UUIDs for armor slots
	private static final UUID[] ARMOR_MODIFIER_IDS = new UUID[]{
			UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), // Feet
			UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), // Legs
			UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), // Chest
			UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")  // Head
	};

	@Override
	public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(
			ItemStack stack,
			Multimap<EntityAttribute, EntityAttributeModifier> vanillaMap,
			EquipmentSlot slot
	) {
		if (stack == null || stack.isEmpty()) {
			return vanillaMap;
		}

		Optional<Component> componentOpt = converter.toComponent(stack);
		if (componentOpt.isEmpty()) {
			return vanillaMap;
		}

		Component component = componentOpt.get();

		// Only equipment components apply their attributes to the player
		if (!(component instanceof EquipmentComponent)) {
			return vanillaMap;
		}

		// Resolve attributes for this equipment
		AttributeQueryResult result = attributeEngine.resolve(component);

		Multimap<EntityAttribute, EntityAttributeModifier> forgeroAttributes = createAttributeMap(result, slot);

		Multimap<EntityAttribute, EntityAttributeModifier> finalMap = LinkedListMultimap.create();
		finalMap.putAll(forgeroAttributes);

		vanillaMap.entries().stream()
				.filter(entry -> !finalMap.containsKey(entry.getKey()))
				.forEach(entry -> finalMap.put(entry.getKey(), entry.getValue()));

		return finalMap;
	}

	private Multimap<EntityAttribute, EntityAttributeModifier> createAttributeMap(AttributeQueryResult attributes, EquipmentSlot slot) {
		Multimap<EntityAttribute, EntityAttributeModifier> map = LinkedListMultimap.create();

		// Handle tool attributes, which apply only in the main hand
		if (slot == EquipmentSlot.MAINHAND) {
			handleAttribute(map, DefaultAttributes.ATTACK_DAMAGE, EntityAttributes.GENERIC_ATTACK_DAMAGE, Item.ATTACK_DAMAGE_MODIFIER_ID, "Forgero Attack Damage", attributes, val -> val - 1.0f, EntityAttributeModifier.Operation.ADDITION);
			handleAttribute(map, DefaultAttributes.ATTACK_SPEED, EntityAttributes.GENERIC_ATTACK_SPEED, Item.ATTACK_SPEED_MODIFIER_ID, "Forgero Attack Speed", attributes, val -> val - 4.0f, EntityAttributeModifier.Operation.ADDITION);
		}

		// Handle armor attributes, which apply in armor slots
		if (slot.getType() == EquipmentSlot.Type.ARMOR) {
			UUID uuid = ARMOR_MODIFIER_IDS[slot.getEntitySlotId()];
			handleAttribute(map, DefaultAttributes.ARMOR, EntityAttributes.GENERIC_ARMOR, uuid, "Forgero Armor", attributes, val -> val, EntityAttributeModifier.Operation.ADDITION);
			handleAttribute(map, DefaultAttributes.ARMOR_TOUGHNESS, EntityAttributes.GENERIC_ARMOR_TOUGHNESS, uuid, "Forgero Armor Toughness", attributes, val -> val, EntityAttributeModifier.Operation.ADDITION);
		}

		return map;
	}

	private void handleAttribute(
			Multimap<EntityAttribute, EntityAttributeModifier> map,
			OpenIdentifier forgeroId,
			EntityAttribute vanillaAttribute,
			UUID modifierUuid,
			String modifierName,
			AttributeQueryResult attributes,
			Function<Float, Float> valueMapper,
			EntityAttributeModifier.Operation operation
	) {
		float value = attributes.getValue(forgeroId);
		if (Math.abs(value) > 0.001f) {
			float modifierValue = valueMapper.apply(value);
			map.put(vanillaAttribute, new EntityAttributeModifier(modifierUuid, modifierName, modifierValue, operation));
		}
	}
}
