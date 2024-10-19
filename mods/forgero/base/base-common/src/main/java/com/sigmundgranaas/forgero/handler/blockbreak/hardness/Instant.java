package com.sigmundgranaas.forgero.handler.blockbreak.hardness;

import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

/**
 * This class allows for instant block breaking.
 * It returns a delta that represents an instant break. Configuration is allowed
 * to determine whether unmineable blocks can also be instantly broken or not.
 */
public class Instant implements BlockBreakSpeedCalculator {
	public final static String TYPE = "forgero:instant";

	public final static ClassKey<Instant> KEY = new ClassKey<>(TYPE, Instant.class);

	public final static JsonBuilder<Instant> BUILDER = HandlerBuilder.fromObjectOrStringDefaulted(KEY.clazz(), TYPE, Instant::fromJson, Instant::new);

	private final boolean canBreakUnmineable;

	public Instant(boolean canBreakUnmineable) {
		this.canBreakUnmineable = canBreakUnmineable;
	}

	public Instant() {
		this.canBreakUnmineable = false;
	}

	public static Instant fromJson(JsonElement jsonElement) {
		boolean canBreakUnmineable = false; // Default value

		if (jsonElement.isJsonObject()) {
			JsonObject json = jsonElement.getAsJsonObject();
			canBreakUnmineable = json.get("can_break_unmineable").getAsBoolean();
		}

		return new Instant(canBreakUnmineable);
	}

	@Override
	public float calculateBlockBreakingDelta(Entity source, BlockPos target, Set<BlockPos> availableBlocks) {
		if (canBreakUnmineable) {
			return 1.0f;
		}

		BlockState state = Utils.getStateFromWorld(source, target);
		if (state.getHardness(source.getWorld(), target) >= 0) {
			return 1.0f;
		}

		return 0.0f;
	}
}
