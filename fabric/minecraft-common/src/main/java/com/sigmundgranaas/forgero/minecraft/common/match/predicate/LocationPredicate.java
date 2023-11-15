package com.sigmundgranaas.forgero.minecraft.common.match.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.block.CampfireBlock;
import net.minecraft.predicate.NumberRange;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.Structure;

@Data
@Builder
public class LocationPredicate {

	public static final LocationPredicate ANY = LocationPredicate.builder().build();
	private static final Logger LOGGER = LogUtils.getLogger();

	@Builder.Default
	private final NumberRange.FloatRange x = NumberRange.FloatRange.ANY;

	@Builder.Default
	private final NumberRange.FloatRange y = NumberRange.FloatRange.ANY;

	@Builder.Default
	private final NumberRange.FloatRange z = NumberRange.FloatRange.ANY;

	@Nullable
	private final RegistryKey<Biome> biome;

	@Nullable
	private final RegistryKey<Structure> feature;

	@Nullable
	private final RegistryKey<World> dimension;

	@Nullable
	private final Boolean smokey;

	@Builder.Default
	private final LightPredicate light = LightPredicate.ANY;

	@Builder.Default
	private final BlockPredicate block = BlockPredicate.ANY;

	@Builder.Default
	private final FluidPredicate fluid = FluidPredicate.ANY;

	public static LocationPredicate feature(RegistryKey<Structure> feature) {
		return LocationPredicate.builder().feature(feature).build();
	}

	public static LocationPredicate y(NumberRange.FloatRange y) {
		return LocationPredicate.builder().y(y).build();
	}

	public static LocationPredicate fromJson(@Nullable JsonElement json) {
		if (json == null || json.isJsonNull()) {
			return ANY;
		}

		JsonObject jsonObject = JsonHelper.asObject(json, "location");

		LocationPredicateBuilder builder = LocationPredicate.builder()
				.x(extractFloatRange(jsonObject, "x"))
				.y(extractFloatRange(jsonObject, "y"))
				.z(extractFloatRange(jsonObject, "z"))
				.dimension(extractRegistryKey(jsonObject, "dimension", Registry.WORLD_KEY))
				.feature(extractRegistryKey(jsonObject, "feature", Registry.STRUCTURE_KEY))
				.biome(extractRegistryKey(jsonObject, "biome", Registry.BIOME_KEY))
				.smokey(jsonObject.has("smokey") ? jsonObject.get("smokey").getAsBoolean() : null)
				.light(LightPredicate.fromJson(jsonObject.get("light")))
				.block(BlockPredicate.fromJson(jsonObject.get("block")))
				.fluid(FluidPredicate.fromJson(jsonObject.get("fluid")));

		return builder.build();
	}

	private static NumberRange.FloatRange extractFloatRange(JsonObject jsonObject, String key) {
		return NumberRange.FloatRange.fromJson(JsonHelper.getObject(jsonObject, "position", new JsonObject()).get(key));
	}

	private static <T> RegistryKey<T> extractRegistryKey(JsonObject jsonObject, String key, RegistryKey<Registry<T>> registryKey) {
		Identifier identifier = getIdentifierFromJson(jsonObject, key);
		return identifier == null ? null : RegistryKey.of(registryKey, identifier);
	}


	private static Identifier getIdentifierFromJson(JsonObject jsonObject, String key) {
		if (jsonObject.has(key)) {
			DataResult<Identifier> result = Identifier.CODEC.parse(JsonOps.INSTANCE, jsonObject.get(key));
			if (result.error().isPresent()) {
				LOGGER.error("Failed to decode " + key + ": " + result.error().get().message());
				return null;
			}
			return result.result().orElse(null);
		}
		return null;
	}

	public boolean test(World world, double x, double y, double z) {
		BlockPos blockPos = new BlockPos(x, y, z);

		if (!isPositionValid(x, y, z)) return false;
		if (!isDimensionValid(world)) return false;
		if (!isBiomeValid(world, blockPos)) return false;
		if (!isFeatureValid(world, blockPos)) return false;
		if (!isSmokeyValid(world, blockPos)) return false;
		if (!this.light.test(world, blockPos)) return false;
		if (!this.block.test(world, blockPos)) return false;

		return this.fluid.test(world, blockPos);
	}

	private boolean isPositionValid(double x, double y, double z) {
		return this.x.test(x) && this.y.test(y) && this.z.test(z);
	}

	private boolean isDimensionValid(World world) {
		return this.dimension == null || this.dimension == world.getRegistryKey();
	}

	private boolean isBiomeValid(World world, BlockPos blockPos) {
		if (this.biome == null) return true;

		boolean canSetBlock = world.canSetBlock(blockPos);
		return canSetBlock && world.getBiome(blockPos).matchesKey(this.biome);
	}

	private boolean isFeatureValid(World world, BlockPos blockPos) {
		if (this.feature == null) return true;

		if (!(world instanceof ServerWorld serverWorld)) return false;

		boolean canSetBlock = serverWorld.canSetBlock(blockPos);
		return canSetBlock && serverWorld.getStructureAccessor().getStructureContaining(blockPos, this.feature).hasChildren();
	}

	private boolean isSmokeyValid(World world, BlockPos blockPos) {
		if (this.smokey == null) return true;

		boolean canSetBlock = world.canSetBlock(blockPos);
		return canSetBlock && this.smokey == CampfireBlock.isLitCampfireInRange(world, blockPos);
	}
}
