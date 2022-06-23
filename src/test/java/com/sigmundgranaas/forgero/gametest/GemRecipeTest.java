package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.item.NBTFactory;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroGemAdapterImpl;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Optional;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;
import static com.sigmundgranaas.forgero.gametest.RecipeHelper.setUpDummyPlayerWithSmithingScreenHandler;

public class GemRecipeTest {
    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Gem testing", required = true)
    public void allGemsCanBeUpgradedToLevel10(TestContext context) {
        ServerPlayerEntity mockPlayer = setUpDummyPlayerWithSmithingScreenHandler(context);
        var gems = ForgeroRegistry.GEM.list();
        int total = gems.size() * 9;
        int correct = gems.stream().map(gem -> {
            int partialSum = 0;
            for (int i = 1; i <= 9; i++) {
                Gem gem1 = gem.createGem(i);
                Gem gem2 = gem.createGem(i);

                ItemStack stack1 = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, gem1.getStringIdentifier())));
                ItemStack stack2 = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, gem2.getStringIdentifier())));

                NBTFactory.INSTANCE.createNBTFromGem(gem1, stack1.getOrCreateNbt());
                NBTFactory.INSTANCE.createNBTFromGem(gem2, stack2.getOrCreateNbt());

                SmithingScreenHandler handler = ((SmithingScreenHandler) mockPlayer.currentScreenHandler);
                handler.setStackInSlot(0, 1, stack1);
                handler.setStackInSlot(1, 1, stack2);
                ItemStack actualOutput = handler.getSlot(2).getStack().copy();
                Optional<Gem> craftedGem = new FabricToForgeroGemAdapterImpl().getGem(actualOutput);

                if (craftedGem.isPresent() && craftedGem.get().getLevel() == i + 1 && craftedGem.get().getStringIdentifier().equals(gem1.getStringIdentifier())
                ) {
                    partialSum++;
                } else if (craftedGem.isEmpty()) {
                    ForgeroInitializer.LOGGER.warn("expected {} to be output, but got {}. There is a possible conflict in the recipes", "a gem", actualOutput.toString());
                }
            }
            return partialSum;
        }).reduce(0, Integer::sum);

        if (total == correct) {
            ForgeroInitializer.LOGGER.info("tested {} gem upgrade recipes, where {}/{} were correct", total, correct, total);
            mockPlayer.closeHandledScreen();
            context.complete();
        } else {
            ForgeroInitializer.LOGGER.error("tested {} gem upgrade recipes, where {}/{} were correct", total, correct, total);
            throw new GameTestException("gem upgrade testing failed");
        }
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Gem testing", required = true)
    public void CannotCombineGemOfDifferentLevel(TestContext context) {
        ServerPlayerEntity mockPlayer = setUpDummyPlayerWithSmithingScreenHandler(context);
        ForgeroRegistry.GEM.list().forEach(gem -> {

            Gem gem1 = gem.createGem(1);
            Gem gem2 = gem.createGem(2);

            ItemStack stack1 = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, gem1.getStringIdentifier())));
            ItemStack stack2 = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, gem2.getStringIdentifier())));

            NBTFactory.INSTANCE.createNBTFromGem(gem1, stack1.getOrCreateNbt());
            NBTFactory.INSTANCE.createNBTFromGem(gem2, stack2.getOrCreateNbt());

            SmithingScreenHandler handler = ((SmithingScreenHandler) mockPlayer.currentScreenHandler);
            handler.setStackInSlot(0, 1, stack1);
            handler.setStackInSlot(1, 1, stack2);
            ItemStack actualOutput = handler.getSlot(2).getStack().copy();
            Optional<Gem> craftedGem = new FabricToForgeroGemAdapterImpl().getGem(actualOutput);

            if (craftedGem.isPresent()
            ) {
                ForgeroInitializer.LOGGER.error("Should not be able to combine {} with level {} and {} with level {}", gem1.getStringIdentifier(), gem1.getLevel(), gem2.getStringIdentifier(), gem2.getLevel());
                throw new GameTestException("gem upgrade testing failed");
            }

        });
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Gem testing", required = true)
    public void allGemsHaveBeenRegistered(TestContext context) {
        ForgeroRegistry.GEM.list().stream().map(gem -> Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, gem.getStringIdentifier()))).forEach(item -> {
            if (item == Items.AIR) {
                throw new GameTestException("Not all gems have been registered correctly");
            }
        });
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Gem testing", required = true)
    public void only1GemPerToolPart(TestContext context) {
        ServerPlayerEntity mockPlayer = setUpDummyPlayerWithSmithingScreenHandler(context);

        ForgeroRegistry.GEM.list().forEach(gem -> {
            SmithingScreenHandler handler = ((SmithingScreenHandler) mockPlayer.currentScreenHandler);

            Gem gem1 = gem.createGem(1);
            Gem gem2 = gem.createGem(2);

            ItemStack gemStack1 = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, gem1.getStringIdentifier())));
            ItemStack gemStack2 = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, gem1.getStringIdentifier())));
            ItemStack toolPartStack = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "iron-pickaxehead")));

            handler.setStackInSlot(0, 1, toolPartStack);
            handler.setStackInSlot(1, 1, gemStack1);
            handler.updateResult();
            ItemStack actualOutput = handler.getSlot(2).getStack().copy();
            if (!(actualOutput.getItem() instanceof ToolPartItem)) {
                throw new GameTestException("Upgrading gem did not succeed");
            }
            handler.setStackInSlot(0, 2, actualOutput);
            handler.updateResult();
            ItemStack airOutput = handler.getSlot(2).getStack();
            if (airOutput.getItem() instanceof ToolPartItem) {
                throw new GameTestException("Should not be able to upgrade with gem if there is already a gem present");
            }
        });

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Gem testing", required = true)
    public void GemUpgradeDoesNotAlterExistingNbtValues(TestContext context) {
        ServerPlayerEntity mockPlayer = setUpDummyPlayerWithSmithingScreenHandler(context);
        SmithingScreenHandler handler = ((SmithingScreenHandler) mockPlayer.currentScreenHandler);


        ItemStack toolPartStack = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "iron-pickaxehead")));
        ItemStack diamondSecondaryUpgrade = new ItemStack(Items.DIAMOND);


        handler.setStackInSlot(0, 1, toolPartStack);
        handler.setStackInSlot(1, 1, diamondSecondaryUpgrade);
        handler.updateResult();
        ItemStack itemStackWithNbtReference1 = handler.getSlot(2).getStack().copy();
        handler.setStackInSlot(0, 2, itemStackWithNbtReference1);

        ItemStack gemStack1 = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, ForgeroRegistry.GEM.list().get(0).getStringIdentifier())));
        handler.setStackInSlot(1, 2, gemStack1);
        handler.updateResult();
        ItemStack itemStackWithNbtReference2 = handler.getSlot(2).getStack().copy();

        if (itemStackWithNbtReference2.getNbt()
                .getCompound(NBTFactory.HEAD_NBT_IDENTIFIER)
                .getString(NBTFactory.SECONDARY_MATERIAL_NBT_IDENTIFIER)
                .equals(itemStackWithNbtReference2.getNbt()
                        .getCompound(NBTFactory.HEAD_NBT_IDENTIFIER)
                        .getString(NBTFactory.SECONDARY_MATERIAL_NBT_IDENTIFIER))) {
            context.complete();

        }
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Gem testing", required = true)
    public void NbtCorrespondToUpgradedLevel(TestContext context) {
        ServerPlayerEntity mockPlayer = setUpDummyPlayerWithSmithingScreenHandler(context);
        SmithingScreenHandler handler = ((SmithingScreenHandler) mockPlayer.currentScreenHandler);


        ItemStack toolPartStack = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "iron-pickaxehead")));
        ItemStack gemStack1 = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, ForgeroRegistry.GEM.list().get(0).getStringIdentifier())));


        handler.setStackInSlot(0, 1, toolPartStack);
        handler.setStackInSlot(1, 1, gemStack1);
        handler.updateResult();
        ItemStack itemStackWithNbtReference1 = handler.getSlot(2).getStack().copy();


        if (Integer.parseInt(itemStackWithNbtReference1.getNbt()
                .getCompound(NBTFactory.HEAD_NBT_IDENTIFIER)
                .getString(NBTFactory.GEM_NBT_IDENTIFIER).split(ELEMENT_SEPARATOR)[0]) == 1
        ) {
            context.complete();
        }
    }
}
