package com.sigmundgranaas.forgero.smithingrework.fluid;

import com.sigmundgranaas.forgero.core.Forgero;

import com.sigmundgranaas.forgero.smithingrework.fluid.custom.MoltenIronFluid;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModFluids {
    public static FlowableFluid STILL_MOLTEN_IRON;
    public static FlowableFluid FLOWING_MOLTEN_IRON;
    public static Block MOLTEN_IRON_BLOCK;
    public static Item MOLTEN_IRON_BUCKET;

	public static boolean isMoltenIron(FluidState state) {
		return state.isOf(ModFluids.STILL_MOLTEN_IRON) || state.isOf(ModFluids.FLOWING_MOLTEN_IRON);
	}

	public static void registerFluid() {
		STILL_MOLTEN_IRON = Registry.register(Registries.FLUID,
				new Identifier(Forgero.NAMESPACE, "molten_iron"), new MoltenIronFluid.Still());
		FLOWING_MOLTEN_IRON = Registry.register(Registries.FLUID,
				new Identifier(Forgero.NAMESPACE, "flowing_molten_iron"), new MoltenIronFluid.Flowing());
		MOLTEN_IRON_BLOCK = Registry.register(Registries.BLOCK, new Identifier(Forgero.NAMESPACE, "molten_iron_block"),
				new FluidBlock(ModFluids.STILL_MOLTEN_IRON, FabricBlockSettings.copyOf(Blocks.LAVA).replaceable()));
		MOLTEN_IRON_BUCKET = Registry.register(Registries.ITEM, new Identifier(Forgero.NAMESPACE, "molten_iron_bucket"),
				new BucketItem(ModFluids.STILL_MOLTEN_IRON, new FabricItemSettings().recipeRemainder(Items.BUCKET).maxCount(1)));

	}
		public static void registerModFluids() {
		Forgero.LOGGER.info("Registering ModFluids for " + Forgero.NAMESPACE);
	}
}
