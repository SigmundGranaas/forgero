package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.item.NBTFactory;
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

import static com.sigmundgranaas.forgero.gametest.RecipeHelper.setUpDummyPlayerWithSmithingScreenHandler;

public class SecondaryUpgradeTest {
    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Secondary upgrade testing", required = true)
    public void secondaryDiamondUpgradeTest(TestContext context) {
        ServerPlayerEntity mockPlayer = setUpDummyPlayerWithSmithingScreenHandler(context);
        SmithingScreenHandler handler = ((SmithingScreenHandler) mockPlayer.currentScreenHandler);


        ItemStack toolPartStack = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "iron_pickaxehead_default")));
        ItemStack diamondSecondaryUpgrade = new ItemStack(Items.DIAMOND);


        handler.setStackInSlot(0, 1, toolPartStack);
        handler.setStackInSlot(1, 1, diamondSecondaryUpgrade);
        handler.updateResult();
        ItemStack itemStackWithNbtReference1 = handler.getSlot(2).getStack().copy();


        if (itemStackWithNbtReference1.getNbt()
                .getCompound(NBTFactory.HEAD_NBT_IDENTIFIER)
                .getString(NBTFactory.SECONDARY_MATERIAL_NBT_IDENTIFIER)
                .equals("diamond")) {
            context.complete();

        } else {
            throw new GameTestException("Secondary material was not applied to tool part");
        }
    }

    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Secondary upgrade testing", required = true)
    public void secondaryStoneUpgradeTest(TestContext context) {
        ServerPlayerEntity mockPlayer = setUpDummyPlayerWithSmithingScreenHandler(context);
        SmithingScreenHandler handler = ((SmithingScreenHandler) mockPlayer.currentScreenHandler);


        ItemStack toolPartStack = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "iron_pickaxehead_default")));
        ItemStack diamondSecondaryUpgrade = new ItemStack(Items.COBBLESTONE);


        handler.setStackInSlot(0, 1, toolPartStack);
        handler.setStackInSlot(1, 1, diamondSecondaryUpgrade);
        handler.updateResult();
        ItemStack itemStackWithNbtReference1 = handler.getSlot(2).getStack().copy();


        if (itemStackWithNbtReference1.getNbt()
                .getCompound(NBTFactory.HEAD_NBT_IDENTIFIER)
                .getString(NBTFactory.SECONDARY_MATERIAL_NBT_IDENTIFIER)
                .equals("stone")) {
            context.complete();

        } else {
            throw new GameTestException("Secondary material was not applied to tool part");
        }
    }
}
