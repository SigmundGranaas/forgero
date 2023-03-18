package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import com.sigmundgranaas.forgero.core.soul.IdentifiableIntTracker;
import com.sigmundgranaas.forgero.core.soul.StatTracker;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.BLOCK_TRACKER_IDENTIFIER;
import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.MOB_TRACKER_IDENTIFIER;

public class StatTrackerParser implements CompoundParser<StatTracker> {
	public static StatTrackerParser PARSER = new StatTrackerParser();

	@Override
	public Optional<StatTracker> parse(NbtCompound element) {
		IdentifiableIntTracker blockTracker = new IdentifiableIntTracker();
		IdentifiableIntTracker mobTracker = new IdentifiableIntTracker();

		if (element.contains(MOB_TRACKER_IDENTIFIER)) {
			mobTracker = new TrackerParser().parse(element.getCompound(MOB_TRACKER_IDENTIFIER)).orElse(new IdentifiableIntTracker());
		}
		if (element.contains(BLOCK_TRACKER_IDENTIFIER)) {
			blockTracker = new TrackerParser().parse(element.getCompound(BLOCK_TRACKER_IDENTIFIER)).orElse(new IdentifiableIntTracker());
		}

		return Optional.of(new StatTracker(blockTracker, mobTracker));
	}

	public static class TrackerParser implements CompoundParser<IdentifiableIntTracker> {

		@Override
		public Optional<IdentifiableIntTracker> parse(NbtCompound element) {
			Map<String, Integer> idMap = new ConcurrentHashMap<>();
			element.getKeys().forEach(key -> {
				if (element.get(key).getType() == NbtElement.INT_TYPE) {
					idMap.put(key, element.getInt(key));
				}
			});
			return Optional.of(new IdentifiableIntTracker(idMap));
		}
	}
}
