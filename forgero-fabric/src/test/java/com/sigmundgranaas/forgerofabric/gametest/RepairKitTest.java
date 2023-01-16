package com.sigmundgranaas.forgerofabric.gametest;

import com.sigmundgranaas.forgerofabric.testutil.RecipeTester;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Supplier;

public class RepairKitTest {


    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
    public void testRepairDiamondPickaxe(TestContext context) {
        var tool = new ItemStack(Registry.ITEM.get(new Identifier("forgero:diamond-pickaxe")));
        tool.setDamage(100);
        var test = RecipeTester.repairKit("diamond_repair_kit", tool, "forgero:diamond-pickaxe",context);
        var result = test.craft();
        if(result.isEmpty()){
            throw new GameTestException("No matching recipe for repair kits");
        } else if(result.get().getDamage() != 0){
            throw new GameTestException("Repair kit did not repair tool");
        }
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "recipe_test", required = true)
    public void testRepairDiamondPickaxeWithFullDurability(TestContext context) {
        var tool = new ItemStack(Registry.ITEM.get(new Identifier("forgero:diamond-pickaxe")));
        var test = RecipeTester.repairKit("diamond_repair_kit", tool, "forgero:diamond-pickaxe",context);
        var result = test.craft();
        if(result.isPresent()){
            throw new GameTestException("Should be able to repair tools with full durability");
        }
        context.complete();
    }

}
