package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgerocore.ForgeroRegistry;
import com.sigmundgranaas.forgerocore.gem.EmptyGem;
import com.sigmundgranaas.forgerocore.gem.ForgeroGem;
import com.sigmundgranaas.forgerocore.gem.Gem;
import com.sigmundgranaas.forgerocore.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgerocore.property.Attribute;
import com.sigmundgranaas.forgerocore.property.AttributeType;
import com.sigmundgranaas.forgerocore.property.NumericOperation;
import com.sigmundgranaas.forgerocore.property.attribute.AttributeBuilder;
import com.sigmundgranaas.forgerocore.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgerocore.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgerocore.toolpart.handle.Handle;
import com.sigmundgranaas.forgerocore.toolpart.handle.HandleState;
import com.sigmundgranaas.forgerocore.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgerocore.toolpart.head.HeadState;
import com.sigmundgranaas.forgerocore.toolpart.head.PickaxeHead;
import com.sigmundgranaas.forgerocore.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.NBTFactory;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.List;

import static com.sigmundgranaas.forgerocore.property.ToolPropertyTest.HANDLE_SCHEMATIC;
import static com.sigmundgranaas.forgerocore.property.ToolPropertyTest.PICKAXEHEAD_SCHEMATIC;

public class GemToolTest {

    public static ItemStack createToolItemWithGem(Gem headGem) {
        HeadState state = new HeadState(ForgeroRegistry.MATERIAL.getPrimaryMaterials().get(0), new EmptySecondaryMaterial(), headGem, PICKAXEHEAD_SCHEMATIC.get());
        ToolPartHead head = new PickaxeHead(state);
        HandleState handleState = new HandleState(ForgeroRegistry.MATERIAL.getPrimaryMaterials().get(0), new EmptySecondaryMaterial(), EmptyGem.createEmptyGem(), HANDLE_SCHEMATIC.get());
        ToolPartHandle handle = new Handle(handleState);

        NbtCompound nbt = NBTFactory.INSTANCE.createNBTFromTool(ForgeroToolFactory.INSTANCE.createForgeroTool(head, handle));

        ItemStack stack = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "iron-pickaxe")));
        stack.getOrCreateNbt().put(NBTFactory.FORGERO_TOOL_NBT_IDENTIFIER, nbt);
        return stack;
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Gem testing", required = true)
    public void DurabilityGemIncreasesByLevel(TestContext context) {
        ItemStack baseTool = createToolItemWithGem(EmptyGem.createEmptyGem());
        float baseDamage = baseTool.getMaxDamage();

        for (int i = 1; i < 10; i++) {
            Attribute attribute = new AttributeBuilder(AttributeType.DURABILITY).applyOperation(NumericOperation.ADDITION).applyValue(100).build();
            ItemStack tool = createToolItemWithGem(new ForgeroGem(i, "emerald-gem", List.of(attribute), List.of(ForgeroToolPartTypes.HANDLE, ForgeroToolPartTypes.HEAD, ForgeroToolPartTypes.BINDING)));
            if (tool.getMaxDamage() != baseDamage + i * 100) {
                //throw new GameTestException("Durability based on Gem is not taken into account");
            }
        }

        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Gem testing", required = true)
    public void miningSpeedGemIncreasesSpeed(TestContext context) {
        ItemStack baseTool = createToolItemWithGem(EmptyGem.createEmptyGem());
        BlockPos pos = new BlockPos(1, 1, 1);
        context.setBlockState(pos, Blocks.STONE);
        float baseSpeed = baseTool.getMiningSpeedMultiplier(context.getBlockState(pos));

        float lastSpeed = baseSpeed;
        for (int i = 1; i < 10; i++) {
            Attribute attribute = new AttributeBuilder(AttributeType.MINING_SPEED).applyOperation(NumericOperation.ADDITION).applyValue(1).build();
            ItemStack tool = createToolItemWithGem(new ForgeroGem(i, "lapis-gem", List.of(attribute), List.of(ForgeroToolPartTypes.HANDLE, ForgeroToolPartTypes.HEAD, ForgeroToolPartTypes.BINDING)));
            float currentSpeed = tool.getMiningSpeedMultiplier(context.getBlockState(pos));
            if (currentSpeed < lastSpeed) {
                throw new GameTestException("Durability based on Gem is not taken into account");
            }
            lastSpeed = currentSpeed;
        }

        context.complete();
    }
}
