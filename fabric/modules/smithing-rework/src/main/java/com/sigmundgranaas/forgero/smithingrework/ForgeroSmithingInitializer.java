package com.sigmundgranaas.forgero.smithingrework;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.smithingrework.block.ModBlocks;
import com.sigmundgranaas.forgero.smithingrework.block.custom.BloomeryBlock2;
import com.sigmundgranaas.forgero.smithingrework.block.entity.BloomeryBlockEntity2;
import com.sigmundgranaas.forgero.smithingrework.block.entity.BloomeryScreenHandler;
import com.sigmundgranaas.forgero.smithingrework.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithingrework.item.custom.LiquidMetalCrucibleItem;
import com.sigmundgranaas.forgero.smithingrework.recipe.MetalSmeltingRecipe;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;


public class ForgeroSmithingInitializer implements ForgeroPreInitializationEntryPoint {
	public static final RegistryKey<ItemGroup> FORGERO_SMITHING_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(Forgero.NAMESPACE, "smithing"));

	public static final Block BLOOMERY_BLOCK2 = new BloomeryBlock2(FabricBlockSettings.copyOf(Blocks.STONE).strength(4.0f).requiresTool());
	public static final BlockItem BLOOMERY_ITEM = new BlockItem(BLOOMERY_BLOCK2, new Item.Settings());
	public static final BlockEntityType<BloomeryBlockEntity2> BLOOMERY_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(BloomeryBlockEntity2::new, BLOOMERY_BLOCK2).build(null);
	public static final ScreenHandlerType<BloomeryScreenHandler> BLOOMERY_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, new Identifier(Forgero.NAMESPACE, "bloomery"), new ScreenHandlerType<>(BloomeryScreenHandler::new, FeatureFlags.VANILLA_FEATURES));
	public static final Item CRUCIBLE = new LiquidMetalCrucibleItem(new Item.Settings());

	public static final Identifier METAL_SMELTING_RECIPE_TYPE_ID = new Identifier(Forgero.NAMESPACE, MetalSmeltingRecipe.ID);


	@Override
	public void onPreInitialization() {
		Registry.register(Registries.BLOCK, new Identifier(Forgero.NAMESPACE, "bloomery"), BLOOMERY_BLOCK2);
		Registry.register(Registries.ITEM, new Identifier(Forgero.NAMESPACE, "bloomery"), BLOOMERY_ITEM);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(Forgero.NAMESPACE, "bloomery"), BLOOMERY_BLOCK_ENTITY);
		Registry.register(Registries.ITEM, new Identifier(Forgero.NAMESPACE, "crucible"), CRUCIBLE);

		// Register Recipe Serializer
		Registry.register(Registries.RECIPE_TYPE, METAL_SMELTING_RECIPE_TYPE_ID, MetalSmeltingRecipe.TYPE);
		Registry.register(Registries.RECIPE_SERIALIZER, METAL_SMELTING_RECIPE_TYPE_ID, MetalSmeltingRecipe.Serializer.INSTANCE);

		ModBlockEntities.registerBlockEntities();
		// ModItemGroups.registerItemGroups();
		// ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		// ModFluids.registerModFluids();
		// registerFluid();
	}


}

