package com.sigmundgranaas.forgero.block.assemblystation;

import com.sigmundgranaas.forgero.Forgero;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class AssemblyStationBlock extends BlockWithEntity {
    public static final Block ASSEMBLY_STATION_BLOCK;
    public static final BlockItem ASSEMBLY_STATION_ITEM;
    public static final BlockEntityType<AssemblyStationEntity> ASSEMBLY_STATION_BLOCK_ENTITY;
    // a public identifier for multiple parts of our bigger chest
    public static final Identifier ASSEMBLY_STATION = new Identifier(Forgero.NAMESPACE, "assembly_station");

    static {
        ASSEMBLY_STATION_BLOCK = Registry.register(Registry.BLOCK, ASSEMBLY_STATION, new AssemblyStationBlock(FabricBlockSettings.copyOf(Blocks.CHEST)));
        ASSEMBLY_STATION_ITEM = Registry.register(Registry.ITEM, ASSEMBLY_STATION, new BlockItem(ASSEMBLY_STATION_BLOCK, new Item.Settings().group(ItemGroup.MISC)));

        // In 1.17 use FabricBlockEntityTypeBuilder instead of BlockEntityType.Builder
        ASSEMBLY_STATION_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, ASSEMBLY_STATION, FabricBlockEntityTypeBuilder.create(AssemblyStationEntity::new, ASSEMBLY_STATION_BLOCK).build(null));
    }

    protected AssemblyStationBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AssemblyStationEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        //With inheriting from BlockWithEntity this defaults to INVISIBLE, so we need to change that!
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            //This will call the createScreenHandlerFactory method from BlockWithEntity, which will return our blockEntity casted to
            //a namedScreenHandlerFactory. If your block class does not extend BlockWithEntity, it needs to implement createScreenHandlerFactory.
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

            if (screenHandlerFactory != null) {
                //With this call the server will request the client to open the appropriate Screenhandler
                player.openHandledScreen(screenHandlerFactory);
            }
        }
        return ActionResult.SUCCESS;
    }


    //This method will drop all items onto the ground when the block is broken
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AssemblyStationEntity) {
                ItemScatterer.spawn(world, pos, ((AssemblyStationEntity) blockEntity).getItems());
                // update comparators
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }
}
