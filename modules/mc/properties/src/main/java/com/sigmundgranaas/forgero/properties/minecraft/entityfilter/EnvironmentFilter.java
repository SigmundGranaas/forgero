package com.sigmundgranaas.forgero.properties.minecraft.entityfilter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Locale;
import java.util.Optional;

/**
 * Filters entities based on environmental conditions such as biome, light level, and weather.
 * All fields are optional - at least one should be specified for the filter to be meaningful.
 *
 * <h3>JSON Configuration Examples:</h3>
 *
 * <p><b>Nether Bonus:</b></p>
 * <pre>
 * {
 *   "type": "forgero:environment",
 *   "biome_tag": "minecraft:is_nether"
 * }
 * </pre>
 *
 * <p><b>Darkness Bonus:</b></p>
 * <pre>
 * {
 *   "type": "forgero:environment",
 *   "light_level_max": 7,
 *   "light_type": "combined"
 * }
 * </pre>
 *
 * <p><b>Night-Only Bonus (Sky Light):</b></p>
 * <pre>
 * {
 *   "type": "forgero:environment",
 *   "light_level_max": 7,
 *   "light_type": "sky"
 * }
 * </pre>
 *
 * <p><b>Rain Bonus:</b></p>
 * <pre>
 * {
 *   "type": "forgero:environment",
 *   "is_raining": true
 * }
 * </pre>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Biome-specific bonuses (damage in Nether, defense in Ocean)</li>
 *   <li>Darkness effects (bonus damage in low light)</li>
 *   <li>Weather-dependent abilities (lightning in rain)</li>
 *   <li>Time-based effects (night vision using sky light)</li>
 * </ul>
 *
 * <h3>Important Notes:</h3>
 * <ul>
 *   <li>Light levels range from 0-15 (0 = darkest, 15 = brightest)</li>
 *   <li>Weather checks always return false in Nether/End (no weather)</li>
 *   <li>BLOCK light = torches, glowstone, etc.</li>
 *   <li>SKY light = sunlight/moonlight</li>
 *   <li>COMBINED light = max(block, sky)</li>
 * </ul>
 *
 * <h3>Performance Notes:</h3>
 * Environmental checks are lightweight. Biome lookups are cached by Minecraft.
 *
 * @param biomeTag Optional biome tag to match (e.g., "minecraft:is_ocean", "minecraft:is_nether")
 * @param lightLevelMin Optional minimum light level (0-15, inclusive). Entity must be AT OR ABOVE this level.
 * @param lightLevelMax Optional maximum light level (0-15, inclusive). Entity must be AT OR BELOW this level.
 * @param lightType Type of light to check (BLOCK, SKY, or COMBINED). Defaults to COMBINED.
 * @param isRaining Optional weather requirement. Note: Always false in Nether/End.
 */
public record EnvironmentFilter(
		Optional<String> biomeTag,
		Optional<Integer> lightLevelMin,
		Optional<Integer> lightLevelMax,
		LightSourceType lightType,
		Optional<Boolean> isRaining
) implements EntityFilter {
	public static final String TYPE = "forgero:environment";
	public static final Codec<EnvironmentFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("biome_tag").forGetter(EnvironmentFilter::biomeTag),
			Codec.intRange(0, 15).optionalFieldOf("light_level_min").forGetter(EnvironmentFilter::lightLevelMin),
			Codec.intRange(0, 15).optionalFieldOf("light_level_max").forGetter(EnvironmentFilter::lightLevelMax),
			LightSourceType.CODEC.optionalFieldOf("light_type", LightSourceType.COMBINED).forGetter(EnvironmentFilter::lightType),
			Codec.BOOL.optionalFieldOf("is_raining").forGetter(EnvironmentFilter::isRaining)
	).apply(instance, EnvironmentFilter::new));

	// Compact constructor for validation
	public EnvironmentFilter {
		// Validate min <= max if both present
		if (lightLevelMin.isPresent() && lightLevelMax.isPresent()) {
			if (lightLevelMin.get() > lightLevelMax.get()) {
				throw new IllegalArgumentException(
					"light_level_min (" + lightLevelMin.get() +
					") must be <= light_level_max (" + lightLevelMax.get() + ")"
				);
			}
		}
	}

	@Override
	public boolean test(Entity source, Entity candidate) {
		World world = candidate.getWorld();
		BlockPos pos = candidate.getBlockPos();

		// Check biome tag with safe identifier parsing
		if (biomeTag.isPresent()) {
			Identifier tagId = Identifier.tryParse(biomeTag.get());
			if (tagId == null) {
				return false; // Invalid identifier format, fail gracefully
			}

			RegistryEntry<Biome> biomeEntry = world.getBiome(pos);
			TagKey<Biome> tag = TagKey.of(RegistryKeys.BIOME, tagId);
			if (!biomeEntry.isIn(tag)) {
				return false;
			}
		}

		// Check light level (optimized to only query once if both min and max present)
		if (lightLevelMin.isPresent() || lightLevelMax.isPresent()) {
			int lightLevel = getLightLevel(world, pos, lightType);

			if (lightLevelMin.isPresent() && lightLevel < lightLevelMin.get()) {
				return false;
			}

			if (lightLevelMax.isPresent() && lightLevel > lightLevelMax.get()) {
				return false;
			}
		}

		// Check weather
		// Note: In dimensions without weather (Nether, End), isRaining() always returns false
		if (isRaining.isPresent()) {
			if (world.isRaining() != isRaining.get()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Gets the light level at the specified position based on the light type.
	 *
	 * @param world The world
	 * @param pos The position to check
	 * @param type The type of light to check
	 * @return Light level 0-15
	 */
	private int getLightLevel(World world, BlockPos pos, LightSourceType type) {
		return switch (type) {
			case BLOCK -> world.getLightLevel(LightType.BLOCK, pos);
			case SKY -> world.getLightLevel(LightType.SKY, pos);
			case COMBINED -> world.getLightLevel(pos);
		};
	}

	@Override
	public String type() {
		return TYPE;
	}

	/**
	 * Types of light that can be checked.
	 */
	public enum LightSourceType implements StringIdentifiable {
		/** Block light from torches, glowstone, etc. */
		BLOCK,
		/** Sky light from sun/moon */
		SKY,
		/** Combined light (max of block and sky) */
		COMBINED;

		public static final Codec<LightSourceType> CODEC = StringIdentifiable.createCodec(
				LightSourceType::values,
				(value) -> String.valueOf(LightSourceType.valueOf(value.toUpperCase(Locale.ROOT)))
		);

		@Override
		public String asString() {
			return this.name().toLowerCase(Locale.ROOT);
		}
	}
}
