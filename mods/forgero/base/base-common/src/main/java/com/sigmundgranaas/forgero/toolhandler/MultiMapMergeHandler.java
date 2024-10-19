package com.sigmundgranaas.forgero.toolhandler;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;

import static net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE;
import static net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_SPEED;

public class MultiMapMergeHandler {

	public Multimap<EntityAttribute, EntityAttributeModifier> modifyAttributeModifiersMap(Multimap<EntityAttribute, EntityAttributeModifier> multimap, EquipmentSlot slot, ItemStack stack, LivingEntity contextEntity) {
		if (stack.getItem() instanceof DynamicAttributeTool holder) {
			// creating a dummy map for our old attributes
			LinkedListMultimap<EntityAttribute, EntityAttributeModifier> oldAttributes = LinkedListMultimap.create();
			oldAttributes.putAll(multimap);
			// Check if the root attributes have changed and remove them
			var newMap = holder.getDynamicModifiers(slot, stack, contextEntity);
			for (EntityAttributeModifier attribute : oldAttributes.get(GENERIC_ATTACK_DAMAGE)) {
				var newValue = newMap.values().stream().filter(newAttribute -> newAttribute.getId() == attribute.getId()).findAny();
				if (newValue.isPresent()) {
					oldAttributes.remove(GENERIC_ATTACK_DAMAGE, attribute);
				}
			}

			for (EntityAttributeModifier attribute : oldAttributes.get(GENERIC_ATTACK_SPEED)) {
				var newValue = newMap.values().stream().filter(newAttribute -> newAttribute.getId() == attribute.getId()).findAny();
				if (newValue.isPresent()) {
					oldAttributes.remove(GENERIC_ATTACK_SPEED, attribute);
				}
			}


			LinkedListMultimap<EntityAttribute, EntityAttributeModifier> orderedAttributes = LinkedListMultimap.create();
			//Place new Forgero root attributes at the start
			orderedAttributes.putAll(newMap);
			//Place old attributes last
			orderedAttributes.putAll(oldAttributes);
			return orderedAttributes;
		}
		return multimap;
	}
}
