package com.sigmundgranaas.forgero.smithing.block.custom;

import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.core.condition.NamedCondition;
import com.sigmundgranaas.forgero.smithing.block.entity.SmithingAnvilBlockEntity;
import com.sigmundgranaas.forgero.smithing.condition.ConditionLootTables;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class SmithingAnvil extends BlockWithEntity implements BlockEntityProvider {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final Identifier SMITHING_ANVIL_ID = new Identifier("forgero", "smithing_anvil");

    private static final VoxelShape SHAPE_NORTH;
    private static final VoxelShape SHAPE_EAST;
    private static final VoxelShape SHAPE_SOUTH;
    private static final VoxelShape SHAPE_WEST;

    private static final Logger LOGGER = LogManager.getLogger(SmithingAnvil.class);

    static {
        VoxelShape shapeS = VoxelShapes.empty();
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.25, 0.5, 0.25, 0.375, 0.5625, 0.75));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.625, 0.5, 0.25, 0.75, 0.5625, 0.75));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.375, 0.5, 0.3125, 0.4375, 0.5625, 0.4375));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.3125, 0.5625, 0.375, 0.375, 0.625, 0.625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.625, 0.5625, 0.375, 0.6875, 0.625, 0.625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.3125, 0.8125, 0.375, 0.6875, 1, 0.625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.6875, 0.875, 0.4375, 0.875, 0.9375, 0.5625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.6875, 0.9375, 0.375, 0.8125, 1, 0.625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.8125, 0.9375, 0.4375, 0.9375, 1, 0.5625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.6875, 0.8125, 0.4375, 0.75, 0.875, 0.5625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.0625, 0.9375, 0.375, 0.1875, 1, 0.625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.1875, 0.875, 0.375, 0.3125, 0.9375, 0.625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.25, 0.8125, 0.375, 0.3125, 0.875, 0.625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.3125, 0.9375, 0.3125, 0.6875, 1, 0.375));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.3125, 0.9375, 0.625, 0.6875, 1, 0.6875));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.4375, 0.5, 0.375, 0.5625, 0.8125, 0.625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.375, 0.5, 0.4375, 0.4375, 0.8125, 0.5625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.5625, 0.5, 0.3125, 0.625, 0.5625, 0.4375));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.5625, 0.5, 0.5625, 0.625, 0.5625, 0.6875));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.375, 0.5, 0.5625, 0.4375, 0.5625, 0.6875));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.375, 0.5625, 0.5625, 0.4375, 0.625, 0.625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.5625, 0.5, 0.4375, 0.625, 0.8125, 0.5625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.5625, 0.5625, 0.5625, 0.625, 0.625, 0.625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.5625, 0.5625, 0.375, 0.625, 0.625, 0.4375));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.375, 0.5625, 0.375, 0.4375, 0.625, 0.4375));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.25, 0.9375, 0.375, 0.3125, 1, 0.625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.1875, 0.9375, 0.5625, 0.25, 1, 0.625));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.1875, 0.9375, 0.375, 0.25, 1, 0.4375));
        shapeS = VoxelShapes.union(shapeS, VoxelShapes.cuboid(0.0625, 0, 0.0625, 0.9375, 0.5, 0.9375));
        SHAPE_SOUTH = shapeS.simplify();

        VoxelShape shapeE = VoxelShapes.empty();
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.25, 0.5, 0.625, 0.75, 0.5625, 0.75));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.25, 0.5, 0.25, 0.75, 0.5625, 0.375));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.3125, 0.5, 0.5625, 0.4375, 0.5625, 0.625));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.5625, 0.625, 0.625, 0.625, 0.6875));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.5625, 0.3125, 0.625, 0.625, 0.375));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.8125, 0.3125, 0.625, 1, 0.6875));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.4375, 0.875, 0.125, 0.5625, 0.9375, 0.3125));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.9375, 0.1875, 0.625, 1, 0.3125));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.4375, 0.9375, 0.0625, 0.5625, 1, 0.1875));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.4375, 0.8125, 0.25, 0.5625, 0.875, 0.3125));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.9375, 0.8125, 0.625, 1, 0.9375));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.875, 0.6875, 0.625, 0.9375, 0.8125));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.8125, 0.6875, 0.625, 0.875, 0.75));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.3125, 0.9375, 0.3125, 0.375, 1, 0.6875));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.625, 0.9375, 0.3125, 0.6875, 1, 0.6875));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.5, 0.4375, 0.625, 0.8125, 0.5625));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.4375, 0.5, 0.5625, 0.5625, 0.8125, 0.625));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.3125, 0.5, 0.375, 0.4375, 0.5625, 0.4375));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.5625, 0.5, 0.375, 0.6875, 0.5625, 0.4375));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.5625, 0.5, 0.5625, 0.6875, 0.5625, 0.625));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.5625, 0.5625, 0.5625, 0.625, 0.625, 0.625));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.4375, 0.5, 0.375, 0.5625, 0.8125, 0.4375));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.5625, 0.5625, 0.375, 0.625, 0.625, 0.4375));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.5625, 0.375, 0.4375, 0.625, 0.4375));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.5625, 0.5625, 0.4375, 0.625, 0.625));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.9375, 0.6875, 0.625, 1, 0.75));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.5625, 0.9375, 0.75, 0.625, 1, 0.8125));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.375, 0.9375, 0.75, 0.4375, 1, 0.8125));
        shapeE = VoxelShapes.union(shapeE, VoxelShapes.cuboid(0.0625, 0, 0.0625, 0.9375, 0.5, 0.9375));
        SHAPE_EAST = shapeE.simplify();

        VoxelShape shapeN = VoxelShapes.empty();
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.625, 0.5, 0.25, 0.75, 0.5625, 0.75));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.25, 0.5, 0.25, 0.375, 0.5625, 0.75));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.5625, 0.5, 0.5625, 0.625, 0.5625, 0.6875));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.625, 0.5625, 0.375, 0.6875, 0.625, 0.625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.3125, 0.5625, 0.375, 0.375, 0.625, 0.625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.3125, 0.8125, 0.375, 0.6875, 1, 0.625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.125, 0.875, 0.4375, 0.3125, 0.9375, 0.5625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.1875, 0.9375, 0.375, 0.3125, 1, 0.625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.0625, 0.9375, 0.4375, 0.1875, 1, 0.5625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.25, 0.8125, 0.4375, 0.3125, 0.875, 0.5625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.8125, 0.9375, 0.375, 0.9375, 1, 0.625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.6875, 0.875, 0.375, 0.8125, 0.9375, 0.625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.6875, 0.8125, 0.375, 0.75, 0.875, 0.625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.3125, 0.9375, 0.625, 0.6875, 1, 0.6875));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.3125, 0.9375, 0.3125, 0.6875, 1, 0.375));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.4375, 0.5, 0.375, 0.5625, 0.8125, 0.625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.5625, 0.5, 0.4375, 0.625, 0.8125, 0.5625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.375, 0.5, 0.5625, 0.4375, 0.5625, 0.6875));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.375, 0.5, 0.3125, 0.4375, 0.5625, 0.4375));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.5625, 0.5, 0.3125, 0.625, 0.5625, 0.4375));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.5625, 0.5625, 0.375, 0.625, 0.625, 0.4375));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.375, 0.5, 0.4375, 0.4375, 0.8125, 0.5625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.375, 0.5625, 0.375, 0.4375, 0.625, 0.4375));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.375, 0.5625, 0.5625, 0.4375, 0.625, 0.625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.5625, 0.5625, 0.5625, 0.625, 0.625, 0.625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.6875, 0.9375, 0.375, 0.75, 1, 0.625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.75, 0.9375, 0.375, 0.8125, 1, 0.4375));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.75, 0.9375, 0.5625, 0.8125, 1, 0.625));
        shapeN = VoxelShapes.union(shapeN, VoxelShapes.cuboid(0.0625, 0, 0.0625, 0.9375, 0.5, 0.9375));
        SHAPE_NORTH = shapeN.simplify();

        VoxelShape shapeW = VoxelShapes.empty();
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.25, 0.5, 0.25, 0.75, 0.5625, 0.375));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.25, 0.5, 0.625, 0.75, 0.5625, 0.75));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.5625, 0.5, 0.375, 0.6875, 0.5625, 0.4375));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.5625, 0.3125, 0.625, 0.625, 0.375));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.5625, 0.625, 0.625, 0.625, 0.6875));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.8125, 0.3125, 0.625, 1, 0.6875));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.4375, 0.875, 0.6875, 0.5625, 0.9375, 0.875));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.9375, 0.6875, 0.625, 1, 0.8125));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.4375, 0.9375, 0.8125, 0.5625, 1, 0.9375));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.4375, 0.8125, 0.6875, 0.5625, 0.875, 0.75));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.9375, 0.0625, 0.625, 1, 0.1875));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.875, 0.1875, 0.625, 0.9375, 0.3125));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.8125, 0.25, 0.625, 0.875, 0.3125));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.625, 0.9375, 0.3125, 0.6875, 1, 0.6875));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.3125, 0.9375, 0.3125, 0.375, 1, 0.6875));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.5, 0.4375, 0.625, 0.8125, 0.5625));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.4375, 0.5, 0.375, 0.5625, 0.8125, 0.4375));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.5625, 0.5, 0.5625, 0.6875, 0.5625, 0.625));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.3125, 0.5, 0.5625, 0.4375, 0.5625, 0.625));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.3125, 0.5, 0.375, 0.4375, 0.5625, 0.4375));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.5625, 0.375, 0.4375, 0.625, 0.4375));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.4375, 0.5, 0.5625, 0.5625, 0.8125, 0.625));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.5625, 0.5625, 0.4375, 0.625, 0.625));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.5625, 0.5625, 0.5625, 0.625, 0.625, 0.625));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.5625, 0.5625, 0.375, 0.625, 0.625, 0.4375));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.9375, 0.25, 0.625, 1, 0.3125));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.375, 0.9375, 0.1875, 0.4375, 1, 0.25));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.5625, 0.9375, 0.1875, 0.625, 1, 0.25));
        shapeW = VoxelShapes.union(shapeW, VoxelShapes.cuboid(0.0625, 0, 0.0625, 0.9375, 0.5, 0.9375));
        SHAPE_WEST = shapeW.simplify();
    }

    public SmithingAnvil(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        return switch (state.get(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> VoxelShapes.fullCube();
        };
    }

    @Override
    public @NotNull BlockState getPlacementState(ItemPlacementContext ctx) {
        @Nullable var placementState = super.getPlacementState(ctx);
        if (placementState == null) {
            return this.getDefaultState();
        }

        return placementState.with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.HORIZONTAL_FACING);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SmithingAnvilBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    private boolean isToolPartHeadOrToolPart(com.sigmundgranaas.forgero.core.type.Type type) {
        if (type.equals(com.sigmundgranaas.forgero.core.type.Type.TOOL_PART_HEAD)
            || type.typeName().equals("TOOL_PART")) {
            return true;
        }
        for (com.sigmundgranaas.forgero.core.type.Type parent : type.parent()) {
            if (isToolPartHeadOrToolPart(parent)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResult onUse(@NotNull BlockState blockState, @NotNull World world, @NotNull BlockPos blockPosition, @Nullable PlayerEntity player, @Nullable Hand hand, @NotNull BlockHitResult blockHitResult) {
        LOGGER.info("SmithingAnvil onUse called");
        if (world.isClient) {
            LOGGER.info("onUse: world is client, returning SUCCESS");
            return ActionResult.SUCCESS;
        }

        BlockEntity blockEntity = world.getBlockEntity(blockPosition);
        if (!(blockEntity instanceof SmithingAnvilBlockEntity smithingAnvilBlockEntity) || player == null) {
            LOGGER.info("onUse: Not a SmithingAnvilBlockEntity or player is null");
            return ActionResult.PASS;
        }

        Inventory inventory = smithingAnvilBlockEntity.getInventory();
        ItemStack stackInHand = player.getStackInHand(hand);
        ItemStack anvilItem = inventory.getStack(0);

        // Generate new marker sequence when a toolpart is placed
        if (!stackInHand.isEmpty() && anvilItem.isEmpty()) {
            smithingAnvilBlockEntity.resetMarkerProgress();
            smithingAnvilBlockEntity.generateSingleMarker();
        }

        // Hammer logic: apply random condition to toolpart after 3 correct hits on markers
        if (stackInHand.getItem().getTranslationKey().contains("smithing_hammer")) {
            LOGGER.info("onUse: Player is holding a smithing hammer");
            if (blockHitResult instanceof BlockHitResult) {
                BlockHitResult bhr = (BlockHitResult) blockHitResult;
                // Convert hit position to local coordinates (relative to block)
                double localX = bhr.getPos().x - blockPosition.getX();
                double localZ = bhr.getPos().z - blockPosition.getZ();
                var markers = smithingAnvilBlockEntity.getMarkerPositions();
                boolean hit = false;
                if (markers.size() == 1) {
                    Vec2f marker = markers.get(0);
                    double dx = marker.x - localX;
                    double dz = marker.y - localZ;
                    if (dx * dx + dz * dz < 0.01) { // threshold squared (0.1^2)
                        smithingAnvilBlockEntity.setMarkerHit(0);
                        hit = true;
                        LOGGER.info("onUse: Marker hit!");
                    }
                }
                smithingAnvilBlockEntity.processMarkerAttempt(hit);
                if (smithingAnvilBlockEntity.getMarkerAttempts() >= 3) {
                    if (!anvilItem.isEmpty()) {
                        var stateOpt = com.sigmundgranaas.forgero.minecraft.common.service.StateService.INSTANCE.convert(anvilItem);
                        if (stateOpt.isPresent() && stateOpt.get() instanceof com.sigmundgranaas.forgero.core.condition.Conditional<?> conditional) {
                            var state = stateOpt.get();
                            if (state instanceof com.sigmundgranaas.forgero.core.state.Typed) {
                                com.sigmundgranaas.forgero.core.state.Typed typed = (com.sigmundgranaas.forgero.core.state.Typed) state;
                                if (isToolPartHeadOrToolPart(typed.type())) {
                                    LOGGER.info("onUse: Toolpart found in anvil: {}", anvilItem);
                                    // Loot table selection based on markerHitsCount
                                    int hits = smithingAnvilBlockEntity.getMarkerHitsCount();
                                    java.util.List<com.sigmundgranaas.forgero.core.condition.NamedCondition> lootTable;
                                    if (hits == 3) {
                                        lootTable = ConditionLootTables.BEST;
                                    } else if (hits == 2) {
                                        lootTable = ConditionLootTables.GOOD;
                                    } else if (hits == 1) {
                                        lootTable = ConditionLootTables.NEUTRAL;
                                    } else if (hits == 0) {
                                        lootTable = ConditionLootTables.BAD;
                                    } else {
                                        lootTable = com.sigmundgranaas.forgero.core.condition.Conditions.INSTANCE.all().stream()
                                                .filter(c -> c instanceof NamedCondition)
                                                .map(c -> (NamedCondition) c)
                                                .collect(Collectors.toList());
                                    }
                                    if (!lootTable.isEmpty()) {
                                        var randomCondition = ConditionLootTables.getRandomCondition(lootTable);
                                        LOGGER.info("onUse: Applying loot table condition: {}", randomCondition.name());
                                        var conditioned = conditional.applyCondition(randomCondition);
                                        var newStackOpt = com.sigmundgranaas.forgero.minecraft.common.service.StateService.INSTANCE.convert((com.sigmundgranaas.forgero.core.state.State)conditioned);
                                        newStackOpt.ifPresent(newStack -> {
                                            LOGGER.info("onUse: Condition applied, updating anvil slot");
                                            inventory.setStack(0, newStack);
                                            smithingAnvilBlockEntity.markDirty();
                                        });
                                    } else {
                                        LOGGER.info("onUse: No conditions available to apply");
                                    }
                                }
                            }
                        }
                    }
                    smithingAnvilBlockEntity.resetMarkerProgress();
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.SUCCESS;
        }

        // Simple logic: right-click with empty hand to pick up, with item to place if empty
        if (stackInHand.isEmpty()) {
            if (!anvilItem.isEmpty()) {
                LOGGER.info("onUse: Picking up item from anvil: {}", anvilItem);
                player.getInventory().offerOrDrop(anvilItem.copy());
                inventory.setStack(0, ItemStack.EMPTY);
                smithingAnvilBlockEntity.markDirty();
                smithingAnvilBlockEntity.resetMarkers();
            }
        } else {
            if (anvilItem.isEmpty()) {
                // Prevent placing items with a condition on the anvil
                var stateOpt = com.sigmundgranaas.forgero.minecraft.common.service.StateService.INSTANCE.convert(stackInHand);
                if (stateOpt.isPresent() && stateOpt.get() instanceof com.sigmundgranaas.forgero.core.condition.Conditional<?> conditional) {
                    if (!conditional.localConditions().isEmpty()) {
                        LOGGER.info("onUse: Tried to place item with condition on anvil, action blocked: {}", stackInHand);
                        if (player != null && world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                            player.sendMessage(net.minecraft.text.Text.literal("That item already has a condition!"), true);
                        }
                        return ActionResult.FAIL;
                    }
                }
                LOGGER.info("onUse: Placing item in anvil: {}", stackInHand);
                ItemStack toPlace = stackInHand.copy();
                toPlace.setCount(1);
                inventory.setStack(0, toPlace);
                LOGGER.info("onUse: Anvil slot now contains: {}", inventory.getStack(0));
                stackInHand.decrement(1);
                smithingAnvilBlockEntity.markDirty();
                LOGGER.info("onUse: Called markDirty after placing item");
                smithingAnvilBlockEntity.generateSingleMarker();
            }
        }
        return ActionResult.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof SmithingAnvilBlockEntity smithingAnvil) {
                ItemScatterer.spawn(world, pos, smithingAnvil.getInventory());
                world.updateComparators(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public <T extends BlockEntity> net.minecraft.block.entity.BlockEntityTicker<T> getTicker(World world, BlockState state, net.minecraft.block.entity.BlockEntityType<T> type) {
        return type == com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities.SMITHING_ANVIL ? (w, pos, s, be) -> {
            if (be instanceof SmithingAnvilBlockEntity anvil) {
                anvil.tick();
            }
        } : null;
    }
}
