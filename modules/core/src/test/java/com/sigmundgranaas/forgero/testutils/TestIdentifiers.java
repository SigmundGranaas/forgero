package com.sigmundgranaas.forgero.testutils;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

/**
 * A centralized repository of Identifiers used across tests.
 * This ensures consistency and reduces string duplication.
 */
public class TestIdentifiers {
	public static final IdentifierFactory idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();

	public static OpenIdentifier id(String name) {
		return idFactory.of(name);
	}

	// Common Component Identifiers
	public static final OpenIdentifier OAK_ID = id("oak");
	public static final OpenIdentifier IRON_ID = id("iron");
	public static final OpenIdentifier GOLD_ID = id("gold");
	public static final OpenIdentifier DIAMOND_ID = id("diamond");
	public static final OpenIdentifier PICKAXE_HEAD_ID = id("pickaxe_head");
	public static final OpenIdentifier AXE_HEAD_ID = id("axe_head");
	public static final OpenIdentifier HANDLE_ID = id("handle");
	public static final OpenIdentifier PICKAXE_ID = id("pickaxe");
	public static final OpenIdentifier SWORD_ID = id("sword");
	public static final OpenIdentifier BLADE_ID = id("blade");
	public static final OpenIdentifier AXE_ID = id("axe");
	public static final OpenIdentifier SHIELD_ID = id("shield");
	public static final OpenIdentifier HILT_ID = id("hilt");
	public static final OpenIdentifier GEM_ID = id("gem");
	public static final OpenIdentifier AMULET_ID = id("amulet");

	// Common Slot Identifiers & Types
	public static final OpenIdentifier MATERIAL_SLOT_TYPE = id("material");
	public static final OpenIdentifier HEAD_SLOT_TYPE = id("pickaxe_head");
	public static final OpenIdentifier HANDLE_SLOT_TYPE = id("handle");
	public static final OpenIdentifier BINDING_SLOT_TYPE = id("binding");
	public static final OpenIdentifier GEM_SLOT_TYPE = id("gem");

	// Common Tags
	public static final OpenIdentifier WOOD_TAG = id("wood");
	public static final OpenIdentifier METAL_TAG = id("metal");
	public static final OpenIdentifier GEM_TAG = id("gem");
	public static final OpenIdentifier PICKAXE_HEAD_TAG = id("pickaxe_head");
	public static final OpenIdentifier HANDLE_TAG = id("handle");
	public static final OpenIdentifier SWORD_BLADE_TAG = id("sword_blade");
	public static final OpenIdentifier HILT_TAG = id("hilt");
	public static final OpenIdentifier POMMEL_TAG = id("pommel");
	public static final OpenIdentifier RUNE_TAG = id("rune");
	public static final OpenIdentifier SWORD_TAG = id("sword");
	public static final OpenIdentifier PICKAXE_TAG = id("pickaxe");
	public static final OpenIdentifier AMULET_TAG = id("amulet");
	public static final OpenIdentifier SHIELD_TAG = id("shield");
	public static final OpenIdentifier AXE_HEAD_TAG = id("axe_head");

	public static final OpenIdentifier DURABILITY_IDENTIFIER = id("durability");
	public static final OpenIdentifier ATTACK_DAMAGE_IDENTIFIER = id("attack_damage");
}
