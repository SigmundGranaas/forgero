package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Filters blocks based on whether they belong to a specific block tag.
 * Useful for tool specialization (e.g., effective vs logs, ores, or stone variants).
 *
 * <h3>JSON Configuration Example:</h3>
 * <pre>
 * {
 *   "type": "forgero:block_tag",
 *   "tag": "minecraft:logs"
 * }
 * </pre>
 *
 * <h3>Common Block Tags:</h3>
 * <ul>
 *   <li>minecraft:logs - All log types</li>
 *   <li>minecraft:leaves - All leaf types</li>
 *   <li>minecraft:planks - All plank types</li>
 *   <li>minecraft:stone_ore_replaceables - Stone types that can have ores</li>
 *   <li>minecraft:base_stone_overworld - Overworld stone types</li>
 *   <li>minecraft:dirt - All dirt variants</li>
 *   <li>c:ores - All ores (Fabric convention tags)</li>
 * </ul>
 *
 * <h3>Use Cases:</h3>
 * <ul>
 *   <li>Vein mining for ores (tag: c:ores)</li>
 *   <li>Tree chopping (tag: minecraft:logs)</li>
 *   <li>Tool specialization (bonus speed for specific block types)</li>
 * </ul>
 *
 * <h3>Performance Notes:</h3>
 * Tag keys are cached at construction time for optimal performance.
 * Tag membership checks are very fast (O(1) hash lookup).
 *
 * @param tag The block tag to match (e.g., "minecraft:logs", "c:ores")
 * @param tagKey Cached tag key for performance (computed from tag)
 */
public record BlockTagFilter(String tag, TagKey<Block> tagKey) implements BlockFilter {
	public static final String TYPE = "forgero:block_tag";

	// Codec only encodes/decodes the tag string, tagKey is computed
	public static final Codec<BlockTagFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("tag").forGetter(BlockTagFilter::tag)
	).apply(instance, BlockTagFilter::new));

	// Constructor from tag string only (used by codec)
	public BlockTagFilter(String tag) {
		this(tag, createTagKey(tag));
	}

	// Compact constructor for validation
	public BlockTagFilter {
		if (tag == null || tag.trim().isEmpty()) {
			throw new IllegalArgumentException("tag cannot be null or empty");
		}
		// tagKey is already validated in createTagKey
	}

	/**
	 * Creates and validates a TagKey from a string identifier.
	 * Validates the identifier format and caches the result.
	 *
	 * @param tag The tag string to parse
	 * @return The parsed and cached TagKey
	 * @throws IllegalArgumentException if tag format is invalid
	 */
	private static TagKey<Block> createTagKey(String tag) {
		Identifier tagId = Identifier.tryParse(tag);
		if (tagId == null) {
			throw new IllegalArgumentException(
				"Invalid tag identifier format: '" + tag + "'. " +
				"Expected format: 'namespace:path' (e.g., 'minecraft:logs')"
			);
		}
		return TagKey.of(RegistryKeys.BLOCK, tagId);
	}

	@Override
	public boolean filter(Entity entity, BlockPos currentPos, BlockPos root) {
		BlockState state = entity.getWorld().getBlockState(currentPos);
		// Use cached tagKey for O(1) lookup performance
		return state.isIn(tagKey);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
