package com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.hardness;

import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.HandlerBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * This class calculates a diminishing return block breaking delta.
 * It applies a diminishing return logic to the block breaking speed calculation,
 * which means each subsequent block in the selection contributes less to the total delta.
 * The class allows for configuring the diminishing factor and base value to tweak
 * the diminishing returns calculation.
 */
public class Diminishing implements BlockBreakSpeedCalculator {
	public final static String TYPE = "forgero:diminishing_return";

	public final static ClassKey<Diminishing> KEY = new ClassKey<>(TYPE, Diminishing.class);

	public final static JsonBuilder<Diminishing> BUILDER = HandlerBuilder.fromObjectOrStringDefaulted(KEY.clazz(), TYPE, Diminishing::fromJson, Diminishing::new);

	private final float diminishingFactor;
	private final float baseValue;

	public Diminishing(float diminishingFactor, float baseValue) {
		this.diminishingFactor = diminishingFactor;
		this.baseValue = baseValue;
	}

	public Diminishing() {
		this.diminishingFactor = 1.0f;
		this.baseValue = 1.0f;
	}

	public static Diminishing fromJson(JsonElement jsonElement) {
		float diminishingFactor = 1.0f; // Default value
		float baseValue = 1.0f; // Default value

		if (jsonElement.isJsonObject()) {
			JsonObject json = jsonElement.getAsJsonObject();
			diminishingFactor = json.get("diminishingFactor").getAsFloat();
			baseValue = json.get("baseValue").getAsFloat();
		}

		return new Diminishing(diminishingFactor, baseValue);
	}

	@Override
	public float calculateBlockBreakingDelta(Entity source, BlockPos target, Set<BlockPos> selectedBlocks) {
		float totalDelta = 0.0f;
		for (BlockPos pos : selectedBlocks) {
			BlockState state = Utils.getStateFromWorld(source, pos);
			float delta = Utils.calculateDelta(state, (PlayerEntity) source, source.getWorld(), pos);
			totalDelta += (float) (delta / (1 + diminishingFactor * Math.pow(delta, baseValue)));
		}
		return totalDelta;
	}
}
