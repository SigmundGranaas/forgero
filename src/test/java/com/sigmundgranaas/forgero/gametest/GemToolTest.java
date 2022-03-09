package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.gem.ForgeroGem;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.properties.Attribute;
import com.sigmundgranaas.forgero.core.properties.AttributeType;
import com.sigmundgranaas.forgero.core.properties.NumericOperation;
import com.sigmundgranaas.forgero.core.properties.attribute.AttributeBuilder;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.handle.Handle;
import com.sigmundgranaas.forgero.core.toolpart.handle.HandleState;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.HeadState;
import com.sigmundgranaas.forgero.core.toolpart.head.PickaxeHead;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.NBTFactory;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.List;

import static com.sigmundgranaas.forgero.gametest.RecipeHelper.setUpDummyPlayerWithSmithingScreenHandler;

public class GemToolTest {

    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Gem testing", required = true)
    public void DurabilityGemIncreasesByLevel(TestContext context) {
        ServerPlayerEntity mockPlayer = setUpDummyPlayerWithSmithingScreenHandler(context);
        ItemStack baseTool = createToolItemWithGem(EmptyGem.createEmptyGem());
        float baseDamage = baseTool.getMaxDamage();

        for (int i = 1; i < 10; i++) {
            Attribute attribute = new AttributeBuilder(AttributeType.DURABILITY).applyOperation(NumericOperation.ADDITION).applyValue(100).build();
            ItemStack tool = createToolItemWithGem(new ForgeroGem(i, "emerald_gem", List.of(attribute), List.of(ForgeroToolPartTypes.HANDLE, ForgeroToolPartTypes.HEAD, ForgeroToolPartTypes.BINDING)));
            if (tool.getMaxDamage() != baseDamage + i * 100) {
                throw new GameTestException("Durability based on Gem is not taken into account");
            }
        }

        context.complete();
    }

    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Gem testing", required = true)
    public void miningSpeedGemIncreasesSpeed(TestContext context) {
        ServerPlayerEntity mockPlayer = setUpDummyPlayerWithSmithingScreenHandler(context);
        ItemStack baseTool = createToolItemWithGem(EmptyGem.createEmptyGem());
        BlockPos pos = new BlockPos(1, 1, 1);
        context.setBlockState(pos, Blocks.STONE);
        float baseSpeed = baseTool.getMiningSpeedMultiplier(context.getBlockState(pos));

        float lastSpeed = baseSpeed;
        for (int i = 1; i < 10; i++) {
            Attribute attribute = new AttributeBuilder(AttributeType.MINING_SPEED).applyOperation(NumericOperation.ADDITION).applyValue(1).build();
            ItemStack tool = createToolItemWithGem(new ForgeroGem(i, "lapis_gem", List.of(attribute), List.of(ForgeroToolPartTypes.HANDLE, ForgeroToolPartTypes.HEAD, ForgeroToolPartTypes.BINDING)));
            float currentSpeed = tool.getMiningSpeedMultiplier(context.getBlockState(pos));
            if (currentSpeed <= lastSpeed) {
                throw new GameTestException("Durability based on Gem is not taken into account");
            }
            lastSpeed = currentSpeed;
        }

        context.complete();
    }

    ItemStack createToolItemWithGem(Gem headGem) {
        HeadState state = new HeadState(ForgeroRegistry.getInstance().materialCollection().getPrimaryMaterialsAsList().get(0), new EmptySecondaryMaterial(), headGem);
        ToolPartHead head = new PickaxeHead(state);
        HandleState handleState = new HandleState(ForgeroRegistry.getInstance().materialCollection().getPrimaryMaterialsAsList().get(0), new EmptySecondaryMaterial(), EmptyGem.createEmptyGem());
        ToolPartHandle handle = new Handle(handleState);

        NbtCompound nbt = NBTFactory.INSTANCE.createNBTFromTool(ForgeroToolFactory.INSTANCE.createForgeroTool(head, handle));

        ItemStack stack = new ItemStack(Registry.ITEM.get(new Identifier(ForgeroInitializer.MOD_NAMESPACE, "iron_pickaxe")));
        stack.getOrCreateNbt().put(NBTFactory.FORGERO_TOOL_NBT_IDENTIFIER, nbt);
        return stack;
    }
}
