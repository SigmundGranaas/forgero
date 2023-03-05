package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.makeshift;

import static net.minecraft.block.Blocks.OAK_WOOD;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.AssemblyStationBlock;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public class MakeshiftAssemblyStationBlock extends AssemblyStationBlock {
	public static final Block MAKESHIFT_ASSEMBLY_STATION_BLOCK = new MakeshiftAssemblyStationBlock(Settings.copy(OAK_WOOD));
	public static final BlockItem MAKESHIFT_ASSEMBLY_STATION_ITEM = new BlockItem(MAKESHIFT_ASSEMBLY_STATION_BLOCK, new Item.Settings().group(ItemGroup.MISC));
	// A public identifier for multiple parts of our bigger chest
	public static final Identifier MAKESHIFT_ASSEMBLY_STATION = new Identifier(Forgero.NAMESPACE, "makeshift_assembly_station");

	protected MakeshiftAssemblyStationBlock(Settings settings) {
		super(settings);
	}
}
