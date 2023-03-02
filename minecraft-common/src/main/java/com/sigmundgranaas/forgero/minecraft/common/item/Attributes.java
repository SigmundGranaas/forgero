package com.sigmundgranaas.forgero.minecraft.common.item;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

public class Attributes {
	public static UUID BASE_MINING_SPEED_ID = UUID.fromString("CB3F55D5-655C-4F38-A497-9C13A33DB5CF");
	public static UUID BASE_DURABILITY_ID = UUID.fromString("CB3F55D5-655C-4F38-A497-9C13A43DB5CF");
	public static UUID BASE_MINING_LEVEL_ID = UUID.fromString("CB3F55D5-655C-4F38-A497-9C13A36DB5CF");
	static EntityAttribute MINING_SPEED;
	static EntityAttribute DURABILITY;
	static EntityAttribute MINING_LEVEL;

	public static void register() {
		MINING_SPEED = Registry.register(Registry.ATTRIBUTE, "generic.mining_speed", (new ClampedEntityAttribute("attribute.name.generic.mining_speed", 1, 0.0, 1024.0)).setTracked(true));
		DURABILITY = Registry.register(Registry.ATTRIBUTE, "generic.durability", (new ClampedEntityAttribute("attribute.name.generic.durability", 1, 0.0, 100000)).setTracked(true));
		MINING_LEVEL = Registry.register(Registry.ATTRIBUTE, "generic.mining_level", (new ClampedEntityAttribute("attribute.name.generic.mining_level", 0, 0.0, 100)).setTracked(true));
	}
}
