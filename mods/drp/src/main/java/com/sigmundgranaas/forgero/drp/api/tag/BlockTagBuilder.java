package com.sigmundgranaas.forgero.drp.api.tag;

import net.minecraft.block.Block;
import net.minecraft.registry.tag.TagKey;

/**
 * Specialized tag builder for block tags with additional type safety.
 */
public interface BlockTagBuilder extends TagBuilder<BlockTagBuilder> {

	/**
	 * Adds a block directly (uses registry lookup).
	 *
	 * @param block The block to add
	 * @return This builder for chaining
	 */
	BlockTagBuilder add(Block block);

	/**
	 * Includes a vanilla block tag.
	 *
	 * @param tagKey The vanilla tag key
	 * @return This builder for chaining
	 */
	BlockTagBuilder includeTag(TagKey<Block> tagKey);
}
