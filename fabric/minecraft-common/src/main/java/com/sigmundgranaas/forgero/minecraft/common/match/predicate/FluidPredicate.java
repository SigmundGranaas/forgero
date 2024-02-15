package com.sigmundgranaas.forgero.minecraft.common.match.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Data;

import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

import org.jetbrains.annotations.Nullable;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Data
@Builder
public class FluidPredicate {

	public static final FluidPredicate ANY = new FluidPredicate(null, null, StatePredicate.ANY);

	@Nullable
	private final TagKey<Fluid> tag;

	@Nullable
	private final Fluid fluid;

	private final StatePredicate state;

	public static FluidPredicate fromJson(@Nullable JsonElement json) {
		if (json == null || json.isJsonNull()) {
			return ANY;
		}

		JsonObject jsonObject = JsonHelper.asObject(json, "fluid");

		Fluid fluid = parseFluid(jsonObject);
		TagKey<Fluid> tag = parseTag(jsonObject);
		StatePredicate state = StatePredicate.fromJson(jsonObject.get("state"));

		return FluidPredicate.builder()
				.tag(tag)
				.fluid(fluid)
				.state(state)
				.build();
	}

	@Nullable
	private static Fluid parseFluid(JsonObject jsonObject) {
		if (!jsonObject.has("fluid")) {
			return null;
		}

		Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "fluid"));
		return Registries.FLUID.get(identifier);
	}

	@Nullable
	private static TagKey<Fluid> parseTag(JsonObject jsonObject) {
		if (!jsonObject.has("tag")) {
			return null;
		}

		Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "tag"));
		return TagKey.of(RegistryKeys.FLUID, identifier);
	}

	public boolean test(World world, BlockPos pos) {
		if (this == ANY) {
			return true;
		}

		if (!world.canSetBlock(pos)) {
			return false;
		}

		FluidState fluidState = world.getFluidState(pos);

		if (tag != null && !fluidState.isIn(tag)) {
			return false;
		}

		if (fluid != null && !fluidState.isOf(fluid)) {
			return false;
		}

		return state.test(fluidState);
	}
}
