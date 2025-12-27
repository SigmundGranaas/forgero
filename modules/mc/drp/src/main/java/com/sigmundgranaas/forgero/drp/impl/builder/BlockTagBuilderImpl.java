package com.sigmundgranaas.forgero.drp.impl.builder;

import com.sigmundgranaas.forgero.drp.api.tag.BlockTagBuilder;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Implementation of BlockTagBuilder.
 */
public class BlockTagBuilderImpl extends AbstractTagBuilder<BlockTagBuilder> implements BlockTagBuilder {

	public BlockTagBuilderImpl(Identifier id) {
		super(id, "blocks");
	}

	@Override
	public BlockTagBuilder add(Block block) {
		Identifier blockId = Registries.BLOCK.getId(block);
		return add(blockId);
	}

	@Override
	public BlockTagBuilder includeTag(TagKey<Block> tagKey) {
		return includeTag(tagKey.id());
	}
}
