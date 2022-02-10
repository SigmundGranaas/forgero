package com.sigmundgranaas.forgero.gametest;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.gem.HeadGem;
import com.sigmundgranaas.forgero.core.gem.gems.AdditiveAttackDamageGemImpl;
import com.sigmundgranaas.forgero.core.gem.gems.AdditiveDurabilityGem;
import com.sigmundgranaas.forgero.core.gem.gems.AdditiveMiningSpeedGem;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.core.toolpart.handle.Handle;
import com.sigmundgranaas.forgero.core.toolpart.handle.HandleState;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.HeadState;
import com.sigmundgranaas.forgero.core.toolpart.head.PickaxeHead;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.NBTFactory;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import static com.sigmundgranaas.forgero.gametest.RecipeHelper.setUpDummyPlayerWithSmithingScreenHandler;

public class GemToolTest {
    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Gem testing", required = true)
    public void AttackDamageGemIncreasesDamageByLevel(TestContext context) {
        ServerPlayerEntity mockPlayer = setUpDummyPlayerWithSmithingScreenHandler(context);

        for (int i = 0; i < 10; i++) {
            ItemStack tool;
            if (i == 0) {
                tool = createToolItemWithGem(EmptyGem.createEmptyGem());
            } else {
                tool = createToolItemWithGem(new AdditiveAttackDamageGemImpl(i, "redstone_gem"));
            }
            mockPlayer.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_AXE));
            ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, 1, 1, 1);
            float healthBefore = zombie.getHealth();
            mockPlayer.attack(zombie);
            mockPlayer.tryAttack(zombie);
            float healthAfter = zombie.getHealth();
            if (healthAfter < healthBefore) {
                context.complete();
            }
            zombie.damage(DamageSource.ANVIL, 100);
        }

        context.complete();
    }

    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Gem testing", required = true)
    public void DurabilityGemIncreasesByLevel(TestContext context) {
        ServerPlayerEntity mockPlayer = setUpDummyPlayerWithSmithingScreenHandler(context);
        ItemStack baseTool = createToolItemWithGem(EmptyGem.createEmptyGem());
        float baseDamage = baseTool.getMaxDamage();

        for (int i = 1; i < 10; i++) {
            ItemStack tool = createToolItemWithGem(new AdditiveDurabilityGem(i, "emerald_gem"));
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
            ItemStack tool = createToolItemWithGem(new AdditiveMiningSpeedGem(i, "lapis_gem"));
            float currentSpeed = tool.getMiningSpeedMultiplier(context.getBlockState(pos));
            if (currentSpeed <= lastSpeed) {
                throw new GameTestException("Durability based on Gem is not taken into account");
            }
            lastSpeed = currentSpeed;
        }

        context.complete();
    }

    ItemStack createToolItemWithGem(Gem headGem) {
        HeadState state = new HeadState(MaterialCollection.INSTANCE.getPrimaryMaterialsAsList().get(0), new EmptySecondaryMaterial(), (HeadGem) headGem, ForgeroToolTypes.PICKAXE);
        ToolPartHead head = new PickaxeHead(state);
        HandleState handleState = new HandleState(MaterialCollection.INSTANCE.getPrimaryMaterialsAsList().get(0), new EmptySecondaryMaterial(), EmptyGem.createEmptyGem());
        ToolPartHandle handle = new Handle(handleState);

        NbtCompound nbt = NBTFactory.INSTANCE.createNBTFromTool(ForgeroToolFactory.INSTANCE.createForgeroTool(head, handle));

        ItemStack stack = new ItemStack(Registry.ITEM.get(new Identifier(Forgero.MOD_NAMESPACE, "iron_pickaxe")));
        stack.getOrCreateNbt().put(NBTFactory.FORGERO_TOOL_NBT_IDENTIFIER, nbt);
        return stack;
    }

    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BasicToolDamage", required = true)
    public void TestBasicToolDamage(TestContext context) {
        ItemStack tool = new ItemStack(Registry.ITEM.get(new Identifier(Forgero.MOD_NAMESPACE, "iron_pickaxe")));
        PlayerEntity mockPlayer = context.createMockPlayer();
        BeeEntity bee = context.spawnEntity(EntityType.BEE, 1, 1, 1);
        float healthBefore = bee.getHealth();
        mockPlayer.setStackInHand(Hand.MAIN_HAND, tool);
        mockPlayer.attack(bee);
        float healthAfter = bee.getHealth();
        if (healthAfter < healthBefore) {
            context.complete();
        }
    }


}
