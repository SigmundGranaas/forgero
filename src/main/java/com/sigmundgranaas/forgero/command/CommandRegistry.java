package com.sigmundgranaas.forgero.command;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.structure.Structure;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.literal;

public class CommandRegistry {
    public void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("forgero").executes(context -> {
                ForgeroInitializer.LOGGER.info("Called Forgero command with no arguments");

                return 1;
            }));

            dispatcher.register(literal("forgero")
                    .then(literal("createstation")
                            .requires(ctx -> ctx.hasPermissionLevel(2))
                            .executes(context -> {
                                BlockPos pos = context.getSource().getPlayer().getBlockPos().add(1, -1, 0);
                                BlockState initialState = context.getSource().getWorld().getBlockState(pos);
                                Optional<Structure> station = context.getSource().getWorld().getStructureManager().getStructure(new Identifier("forgero:crafting_station"));
                                context.getSource().getWorld().setBlockState(pos, Blocks.STRUCTURE_BLOCK.getDefaultState());
                                if (station.isPresent() && !context.getSource().getWorld().isClient) {
                                    var structureBlock = new StructureBlockBlockEntity(pos, context.getSource().getWorld().getBlockState(pos));
                                    structureBlock.setStructureName(new Identifier("forgero:crafting_station"));
                                    structureBlock.loadStructure(context.getSource().getWorld());
                                    structureBlock.place(context.getSource().getWorld(), true, station.get());
                                    context.getSource().sendFeedback(new LiteralText("Placed Forgero testing station"), true);
                                    context.getSource().getWorld().setBlockState(pos, initialState);
                                    //station.get().place(context.getSource().getWorld(), pos, pos, new StructurePlacementData(), new Random(), 0);
                                }
                                return 1;
                            })
                    )
            );
        });
    }

}
