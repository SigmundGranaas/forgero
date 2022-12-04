package com.sigmundgranaas.forgero.block.assemblystation;

import com.sigmundgranaas.forgero.Forgero;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class AssemblyStationBlock extends HorizontalFacingBlock {
    public static final EnumProperty<AssemblyStationPart> PART = EnumProperty.of("part", AssemblyStationPart.class);
    public static final Block ASSEMBLY_STATION_BLOCK;
    public static final BlockItem ASSEMBLY_STATION_ITEM;
    // a public identifier for multiple parts of our bigger chest
    public static final Identifier ASSEMBLY_STATION = new Identifier(Forgero.NAMESPACE, "assembly_station");

    static {
        ASSEMBLY_STATION_BLOCK = Registry.register(Registry.BLOCK, ASSEMBLY_STATION, new AssemblyStationBlock(AbstractBlock.Settings.of(Material.METAL)));
        ASSEMBLY_STATION_ITEM = Registry.register(Registry.ITEM, ASSEMBLY_STATION, new BlockItem(ASSEMBLY_STATION_BLOCK, new Item.Settings().group(ItemGroup.MISC)));
    }

    protected AssemblyStationBlock(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(PART, AssemblyStationPart.LEFT));
        this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(PART, AssemblyStationPart.LEFT);
    }

    public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, VoxelShapes.empty()};

        int times = (to.getHorizontal() - from.getHorizontal() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = VoxelShapes.union(buffer[1], VoxelShapes.cuboid(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = VoxelShapes.empty();
        }

        return buffer[0];
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return true;
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

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getPlayerFacing().getOpposite();
        return this.getDefaultState().with(FACING, direction);
    }

    @Override
    public NamedScreenHandlerFactory createScreenHandlerFactory(BlockState state, World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> new AssemblyStationScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos)), Text.literal("assembly_station"));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            BlockPos blockPos = pos.offset(state.get(FACING).rotateCounterclockwise(Direction.Axis.Y));
            world.setBlockState(blockPos, state.with(PART, AssemblyStationPart.RIGHT), 3);
            world.updateNeighbors(pos, Blocks.AIR);
            state.updateNeighbors(world, pos, 3);
        }
    }

    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && player.isCreative()) {
            BlockPos blockPos = pos.offset(state.get(FACING).rotateCounterclockwise(Direction.Axis.Y));
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
            world.updateNeighbors(pos, Blocks.AIR);
            state.updateNeighbors(world, pos, 3);
        }

        super.onBreak(world, pos, state, player);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (super.canPlaceAt(state, world, pos)) {
            BlockPos blockPos = pos.offset(state.get(FACING).rotateCounterclockwise(Direction.Axis.Y));
            return world.getBlockState(blockPos).isAir();
        } else {
            return false;
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        if (state.get(PART) == AssemblyStationPart.RIGHT) {
            return BlockRenderType.INVISIBLE;
        }
        return super.getRenderType(state);
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    public VoxelShape makeShape() {
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.25, 0.625, 0, 1.75, 0.75, 1));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0.375, 0.0625, 1.6875, 0.625, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.5625, 0, 0.0625, 1.6875, 0.375, 0.1875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(1.5625, 0, 0.8125, 1.6875, 0.375, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0, 0.8125, 0.4375, 0.375, 0.9375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.3125, 0, 0.0625, 0.4375, 0.375, 0.1875));

        return shape;
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        AssemblyStationPart part = state.get(PART);
        if (part == AssemblyStationPart.LEFT) {
            return rotateShape(Direction.SOUTH, state.get(FACING), makeShape());
        }
        return VoxelShapes.empty();
    }

    public enum AssemblyStationPart implements StringIdentifiable {
        RIGHT("right"),
        LEFT("left");

        private final String name;

        private AssemblyStationPart(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }

        public String asString() {
            return this.name;
        }
    }
}
