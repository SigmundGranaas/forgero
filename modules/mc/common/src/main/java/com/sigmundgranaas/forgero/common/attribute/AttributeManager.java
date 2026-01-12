package com.sigmundgranaas.forgero.common.attribute;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeQueryResult;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.EquipmentComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * The central manager for providing Forgero-calculated attributes to Minecraft.
 * This class is initialized once by the ForgeroDataLoader and then serves as the main entry point
 * for the various attribute-related mixins.
 */
public class AttributeManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AttributeManager.class);
	private static ComponentConverter converter;
	private static boolean initialized = false;

	// Vanilla attribute modifier UUIDs, essential for replacing vanilla modifiers.
	private static final UUID[] ARMOR_MODIFIER_IDS = new UUID[]{
			UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), // Feet
			UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), // Legs
			UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), // Chest
			UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")  // Head
	};

	/**
	 * Initializes the manager with necessary services from the loader.
	 */
	public static void initialize(ComponentConverter converter) {
		if (initialized) {
			LOGGER.warn("ForgeroAttributeManager is being initialized more than once. " +
					"This may indicate a mod lifecycle issue - the attribute manager should only be initialized once during mod setup.");
			return;
		}
		AttributeManager.converter = converter;
		AttributeManager.initialized = true;
	}

	/**
	 * Shared helper method to get the resolved AttributeQueryResult for any ItemStack.
	 * This is used by all attribute-related mixins.
	 *
	 * @param stack The ItemStack to resolve.
	 * @return An Optional containing the AttributeQueryResult if resolution is successful.
	 */
	public static Optional<AttributeQueryResult> getResolvedAttributes(ItemStack stack) {
		if (!initialized || stack.isEmpty()) {
			return Optional.empty();
		}
		return converter.toComponent(stack)
				.map(component -> new AttributeEngine().resolve(component));
	}

	/**
	 * Public helper to get a Component from an ItemStack, for use in mixins.
	 *
	 * @param stack The item stack to convert.
	 * @return An Optional containing the Component if conversion is successful.
	 */
	public static Optional<Component> getComponent(ItemStack stack) {
		if (!initialized || stack.isEmpty()) {
			return Optional.empty();
		}
		return converter.toComponent(stack);
	}


	/**
	 * The main method for retrieving EntityAttributes for an ItemStack.
	 * It uses the shared helper to resolve attributes and then builds the Multimap.
	 * <p>
	 * <b>Important:</b> Only {@link EquipmentComponent}s apply their attributes to players.
	 * Parts and materials ({@link com.sigmundgranaas.forgero.core.component.api.ContributingComponent})
	 * expose attributes for inspection and composition but do not apply them when held.
	 */
	public static Multimap<EntityAttribute, EntityAttributeModifier> getAttributes(ItemStack stack, Multimap<EntityAttribute, EntityAttributeModifier> vanillaMap, EquipmentSlot slot) {
		if (!initialized || stack.isEmpty()) {
			return vanillaMap;
		}

		// Get the component and check if it's equipment (terminal component)
		Optional<Component> componentOpt = getComponent(stack);
		if (componentOpt.isEmpty()) {
			return vanillaMap;
		}

		Component component = componentOpt.get();

		// CRITICAL: Only equipment components apply their attributes to the player.
		// Parts and materials (ContributingComponent) provide attributes for composition
		// and inspection only - they don't modify player stats when held.
		if (!(component instanceof EquipmentComponent)) {
			return vanillaMap;
		}

		// Resolve attributes for this equipment
		AttributeQueryResult result = new AttributeEngine().resolve(component);

		Multimap<EntityAttribute, EntityAttributeModifier> forgeroAttributes = createAttributeMap(result, slot);

		Multimap<EntityAttribute, EntityAttributeModifier> finalMap = LinkedListMultimap.create();
		finalMap.putAll(forgeroAttributes);

		vanillaMap.entries().stream()
				.filter(entry -> !finalMap.containsKey(entry.getKey()))
				.forEach(entry -> finalMap.put(entry.getKey(), entry.getValue()));

		return finalMap;
	}

	private static Multimap<EntityAttribute, EntityAttributeModifier> createAttributeMap(AttributeQueryResult attributes, EquipmentSlot slot) {
		Multimap<EntityAttribute, EntityAttributeModifier> map = LinkedListMultimap.create();

		// Handle tool attributes, which apply only in the main hand.
		if (slot == EquipmentSlot.MAINHAND) {
			// Attack damage: Forgero stores total damage (e.g., 7 for diamond sword).
			// Minecraft's ADDITION modifier adds to base player damage (1.0).
			// So we subtract 1 to get the correct modifier value (7 - 1 = 6).
			handleAttribute(map, DefaultAttributes.ATTACK_DAMAGE, EntityAttributes.GENERIC_ATTACK_DAMAGE, Item.ATTACK_DAMAGE_MODIFIER_ID, "Forgero Attack Damage", attributes, val -> val - 1.0f, EntityAttributeModifier.Operation.ADDITION);
			handleAttribute(map, DefaultAttributes.ATTACK_SPEED, EntityAttributes.GENERIC_ATTACK_SPEED, Item.ATTACK_SPEED_MODIFIER_ID, "Forgero Attack Speed", attributes, val -> val - 4.0f, EntityAttributeModifier.Operation.ADDITION); // Value is desired speed, modifier is delta from base 4.0
		}

		// Handle armor attributes, which apply in armor slots.
		if (slot.getType() == EquipmentSlot.Type.ARMOR) {
			UUID uuid = ARMOR_MODIFIER_IDS[slot.getEntitySlotId()];
			handleAttribute(map, DefaultAttributes.ARMOR, EntityAttributes.GENERIC_ARMOR, uuid, "Forgero Armor", attributes, val -> val, EntityAttributeModifier.Operation.ADDITION);
			handleAttribute(map, DefaultAttributes.ARMOR_TOUGHNESS, EntityAttributes.GENERIC_ARMOR_TOUGHNESS, uuid, "Forgero Armor Toughness", attributes, val -> val, EntityAttributeModifier.Operation.ADDITION);
		}

		return map;
	}

	private static void handleAttribute(
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
