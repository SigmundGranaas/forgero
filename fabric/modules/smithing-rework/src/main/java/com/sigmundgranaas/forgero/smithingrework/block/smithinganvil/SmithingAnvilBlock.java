package com.sigmundgranaas.forgero.smithingrework.block.smithinganvil;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class SmithingAnvilBlock extends Block {
	public static final Block SMITHING_ANVIL_BLOCK = new SmithingAnvilBlock(
			AbstractBlock.Settings.copy(Blocks.ANVIL).strength(2.5F).sounds(BlockSoundGroup.WOOD));
	public static final Item SMITHING_ANVIL_ITEM = new BlockItem(SMITHING_ANVIL_BLOCK, new Item.Settings());
	// TODO: Move Forgero identifier into a Forgero.NAMESPACE variable that is accessible from the smithing-rework module
	public static final Identifier SMITHING_ANVIL = new Identifier("forgero", "smithing_anvil");

	public SmithingAnvilBlock(AbstractBlock.Settings settings) {
		super(settings);
	}
}
