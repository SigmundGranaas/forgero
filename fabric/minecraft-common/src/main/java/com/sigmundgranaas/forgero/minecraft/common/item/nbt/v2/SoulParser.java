package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.*;

import java.util.Optional;

import com.sigmundgranaas.forgero.core.registry.SoulLevelPropertyRegistry;
import com.sigmundgranaas.forgero.core.soul.Soul;
import com.sigmundgranaas.forgero.core.soul.SoulSource;
import com.sigmundgranaas.forgero.core.soul.StatTracker;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class SoulParser implements CompoundParser<Soul> {
	public static SoulParser PARSER = new SoulParser();

	public static Optional<Soul> of(ItemStack stack) {
		if (stack.hasNbt() && stack.getOrCreateNbt().contains(FORGERO_IDENTIFIER) && stack.getOrCreateNbt().getCompound(FORGERO_IDENTIFIER).contains(SOUL_IDENTIFIER)) {
			return SoulParser.PARSER.parse(stack.getOrCreateNbt().getCompound(FORGERO_IDENTIFIER).getCompound(SOUL_IDENTIFIER));
		}
		return Optional.empty();
	}

	@Override
	public Optional<Soul> parse(NbtCompound element) {
		if (element.contains(LEVEL_IDENTIFIER) && element.contains(XP_IDENTIFIER) && element.contains(NAME_IDENTIFIER)) {
			int level = element.getInt(LEVEL_IDENTIFIER);
			int xp = element.getInt(XP_IDENTIFIER);
			String name = element.getString(NAME_IDENTIFIER);
			String id = element.getString(ID_IDENTIFIER);
			StatTracker tracker;
			if (element.contains(TRACKER_IDENTIFIER)) {
				tracker = StatTrackerParser.PARSER.parse(element.getCompound(TRACKER_IDENTIFIER)).orElse(new StatTracker());
			} else {
				tracker = new StatTracker();
			}
			return Optional.of(new Soul(level, xp, new SoulSource(id, name), tracker, SoulLevelPropertyRegistry.handler()));
		}
		return Optional.empty();
	}
}
