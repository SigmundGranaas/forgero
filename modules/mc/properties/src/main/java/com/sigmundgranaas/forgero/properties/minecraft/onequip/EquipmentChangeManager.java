package com.sigmundgranaas.forgero.properties.minecraft.onequip;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.runtime.ContextKeys;
import com.sigmundgranaas.forgero.common.runtime.DynamicContext;
import com.sigmundgranaas.forgero.common.runtime.MinecraftContextKeys;
import com.sigmundgranaas.forgero.common.runtime.PropertyDispatcher;
import com.sigmundgranaas.forgero.properties.minecraft.EntityEffects;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

/**
 * Detects equipment changes per tick by diffing every equipment slot (armor + both hands) against
 * the previous snapshot, firing {@code on_equip}/{@code on_unequip} on the changed stacks. Mirrors
 * the per-UUID state pattern used by {@code OnSneakToggleManager}. Change detection is by item
 * identity, so a mere durability tick does not re-fire; snapshots are only re-copied when a slot
 * actually changes, so the steady-state cost is just item comparisons.
 */
public class EquipmentChangeManager {
	private static final EquipmentSlot[] SLOTS = EquipmentSlot.values();
	private static final ConcurrentHashMap<UUID, ItemStack[]> lastArmor = new ConcurrentHashMap<>();

	static {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> cleanup(handler.player.getUuid()));
		}
	}

	private EquipmentChangeManager() {
	}

	public static void handleTick(LivingEntity entity) {
		if (entity.getWorld().isClient()) {
			return;
		}

		ItemStack[] current = new ItemStack[SLOTS.length];
		for (int i = 0; i < SLOTS.length; i++) {
			current[i] = entity.getEquippedStack(SLOTS[i]);
		}

		ItemStack[] previous = lastArmor.get(entity.getUuid());
		// First observation: snapshot only, so we don't spuriously fire on login/chunk load.
		if (previous == null || previous.length != current.length) {
			lastArmor.put(entity.getUuid(), snapshot(current));
			return;
		}

		ItemStack[] updated = previous;
		boolean changed = false;
		for (int i = 0; i < current.length; i++) {
			ItemStack prev = previous[i];
			ItemStack cur = current[i];
			if (!itemChanged(prev, cur)) {
				continue;
			}
			if (!changed) {
				updated = previous.clone();
				changed = true;
			}
			if (!prev.isEmpty()) {
				fireUnequip(entity, prev);
			}
			if (!cur.isEmpty()) {
				fireEquip(entity, cur);
			}
			updated[i] = cur.copy();
		}

		if (changed) {
			lastArmor.put(entity.getUuid(), updated);
		}
	}

	private static boolean itemChanged(ItemStack a, ItemStack b) {
		if (a.isEmpty() && b.isEmpty()) {
			return false;
		}
		if (a.isEmpty() != b.isEmpty()) {
			return true;
		}
		return a.getItem() != b.getItem();
	}

	private static ItemStack[] snapshot(ItemStack[] stacks) {
		ItemStack[] array = new ItemStack[stacks.length];
		for (int i = 0; i < stacks.length; i++) {
			array[i] = stacks[i].copy();
		}
		return array;
	}

	private static void fireEquip(LivingEntity entity, ItemStack stack) {
		DynamicContext ctx = buildContext(entity);
		for (OnEquipProperty property : PropertyDispatcher.active(stack, OnEquipProperty.KEY, ctx)) {
			EntityEffects.apply(property.selector(), property.effects(), entity, entity);
		}
	}

	private static void fireUnequip(LivingEntity entity, ItemStack stack) {
		DynamicContext ctx = buildContext(entity);
		for (OnUnequipProperty property : PropertyDispatcher.active(stack, OnUnequipProperty.KEY, ctx)) {
			EntityEffects.apply(property.selector(), property.effects(), entity, entity);
		}
	}

	private static DynamicContext buildContext(LivingEntity entity) {
		DynamicContext.Builder builder = new DynamicContext.Builder();
		Set<OpenIdentifier> entityTags = Registries.ENTITY_TYPE.getEntry(entity.getType())
				.streamTags()
				.map(TagKey::id)
				.map(id -> new OpenIdentifier(id.getNamespace(), id.getPath()))
				.collect(Collectors.toSet());
		builder.put(ContextKeys.TARGET_TAGS, entityTags);
		builder.put(MinecraftContextKeys.SOURCE_ENTITY, entity);
		builder.put(MinecraftContextKeys.TARGET_ENTITY, entity);
		builder.put(MinecraftContextKeys.WORLD, entity.getWorld());
		return builder.build();
	}

	public static void cleanup(UUID entityId) {
		lastArmor.remove(entityId);
	}

	public static void clearAll() {
		lastArmor.clear();
	}
}
