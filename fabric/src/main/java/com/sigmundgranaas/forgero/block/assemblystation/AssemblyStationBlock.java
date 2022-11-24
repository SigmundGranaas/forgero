package com.sigmundgranaas.forgero.block.assemblystation;

import com.sigmundgranaas.forgero.Forgero;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class AssemblyStationBlock extends Block {
    public static final Block ASSEMBLY_STATION_BLOCK;
    public static final BlockItem ASSEMBLY_STATION_ITEM;

    // a public identifier for multiple parts of our bigger chest
    public static final Identifier ASSEMBLY_STATION = new Identifier(Forgero.NAMESPACE, "assembly_station");

    static {
        ASSEMBLY_STATION_BLOCK = Registry.register(Registry.BLOCK, ASSEMBLY_STATION, new AssemblyStationBlock(FabricBlockSettings.copyOf(Blocks.CHEST)));
        ASSEMBLY_STATION_ITEM = Registry.register(Registry.ITEM, ASSEMBLY_STATION, new BlockItem(ASSEMBLY_STATION_BLOCK, new Item.Settings().group(ItemGroup.MISC)));
    }

    protected AssemblyStationBlock(Settings settings) {
        super(settings);
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

    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> new AssemblyStationScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)), Text.literal("assembly_station"));
    }


    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return makeShape();
    }

    public VoxelShape makeShape() {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.0625, 0, 0.0625, 1.1875, 0.375, 0.1875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.3125, 0.6875, 0, 1.375, 0.75, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(-0.1875, 0.375, 0.0625, 1.1875, 0.625, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(-0.25, 0.625, 0, 1.25, 0.75, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.25, 0.6875, 0, 1.3125, 0.75, 0.0625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.25, 0.6875, 0.9375, 1.3125, 0.75, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(-0.3125, 0.5625, 0.375, -0.0625, 0.8125, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(-0.3125, 0.8125, 0.375, -0.1875, 0.9375, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(-0.1875, 0.875, 0.375, -0.0625, 0.9375, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.0625, 0, 0.8125, 1.1875, 0.375, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(-0.1875, 0, 0.8125, -0.0625, 0.375, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(-0.1875, 0, 0.0625, -0.0625, 0.375, 0.1875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.0625, 0.09375, 0.9381249999999999, 0.9375, 0.59375, 0.9381249999999999));

        return shape;
    }

}
