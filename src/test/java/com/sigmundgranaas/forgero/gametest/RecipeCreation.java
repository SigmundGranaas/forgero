package com.sigmundgranaas.forgero.gametest;

import com.google.gson.JsonArray;
import com.mojang.authlib.GameProfile;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleDuoMaterial;
import com.sigmundgranaas.forgero.core.material.material.simple.SimpleMaterialPOJO;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.core.toolpart.handle.ToolPartHandle;
import com.sigmundgranaas.forgero.core.toolpart.head.ToolPartHead;
import com.sigmundgranaas.forgero.item.ItemCollection;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.item.adapter.ToolMaterialAdapter;
import com.sigmundgranaas.forgero.item.tool.ForgeroPickaxeItem;
import com.sigmundgranaas.forgero.recipe.RecipeCollection;
import com.sigmundgranaas.forgero.recipe.RecipeWrapper;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Blocks;
import net.minecraft.block.CraftingTableBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.GameTestException;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RecipeCreation {

    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BasicToolDamage", required = true)
    public void TestBasicToolDamage(TestContext context) {
        ItemStack tool = new ItemStack(createDummyToolItem());
        PlayerEntity mockPlayer = context.createMockPlayer();
        BeeEntity bee = context.spawnEntity(EntityType.BEE, 0, 0, 0);
        float healthBefore = bee.getHealth();
        mockPlayer.setStackInHand(Hand.MAIN_HAND, tool);
        mockPlayer.attack(bee);
        float healthAfter = bee.getHealth();
        if (healthAfter < healthBefore) {
            context.complete();
        }
    }

    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "BasicToolDamage")
    public void testSomeThingRandom(TestContext context) {
        ItemStack tool = new ItemStack(Items.GOLDEN_SHOVEL);
        PlayerEntity mockPlayer = context.createMockPlayer();
        //mockPlayer.setStackInHand(Hand.MAIN_HAND, tool);
        ItemCollection.INSTANCE.getToolItems().stream().filter(pickaxe -> pickaxe instanceof PickaxeItem).forEach(toolItem -> {
            //((PickaxeItem) toolItem).asItem())
            mockPlayer.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
            if (mockPlayer.canHarvest(Blocks.BEDROCK.getDefaultState())) {
                //throw new GameTestException("should'nt");
            }
            mockPlayer.clearActiveItem();
        });
        context.complete();
    }


    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Recipe testing")
    public void testCraftAllHandles(TestContext context) {
        ServerPlayerEntity mockPlayer = createDummyServerPlayer(context);
        context.setBlockState(new BlockPos(0, 0, 0), Blocks.CRAFTING_TABLE);
        CraftingTableBlock craftingTableBlock = (CraftingTableBlock) context.getBlockState(new BlockPos(0, 0, 0)).getBlock();
        mockPlayer.currentScreenHandler = craftingTableBlock.createScreenHandlerFactory(craftingTableBlock.getDefaultState(), context.getWorld(), new BlockPos(0, 0, 0)).createMenu(0, mockPlayer.getInventory(), mockPlayer);
        int total = 0;
        int correct = 0;
        for (Item toolPart : ItemCollection.INSTANCE.getToolParts().stream().filter(toolPart -> ((ToolPartItem) toolPart).getType() == ForgeroToolPartTypes.HANDLE).collect(Collectors.toList())
        ) {
            Item output = testHandleRecipe(Registry.ITEM.get(new Identifier(((ToolPartItem) toolPart).getPrimaryMaterial().getIngredient())), mockPlayer);
            if (output instanceof ToolPartItem && ((ToolPartItem) output).getPart().getToolPartIdentifier().equals(((ToolPartItem) toolPart).getPart().getToolPartIdentifier())) {
                total++;
                correct++;
            } else {
                total++;
                Forgero.LOGGER.error("Expected {}, but got {}", ((ToolPartItem) toolPart).getIdentifier(), output.asItem().getName());
                //throw new GameTestException(String.format("Crafting recipe for %s is bad", ((ToolPartItem) toolPart).getIdentifier()));
            }
        }

        if (total == correct) {
            Forgero.LOGGER.info("tested {} recipes, where {}/{} were correct", total, total, correct);
            mockPlayer.closeHandledScreen();
            context.complete();
        } else {
            Forgero.LOGGER.info("tested {} recipes, where {}/{} were correct", total, total, correct);
            throw new GameTestException("recipe testing failed");
        }
    }

    @GameTest(structureName = FabricGameTest.EMPTY_STRUCTURE, batchId = "Recipe testing", required = false)
    public void testingGeneratedRecipes(TestContext context) {
        ServerPlayerEntity mockPlayer = createDummyServerPlayer(context);
        context.setBlockState(new BlockPos(0, 0, 0), Blocks.CRAFTING_TABLE);
        CraftingTableBlock craftingTableBlock = (CraftingTableBlock) context.getBlockState(new BlockPos(0, 0, 0)).getBlock();
        mockPlayer.currentScreenHandler = craftingTableBlock.createScreenHandlerFactory(craftingTableBlock.getDefaultState(), context.getWorld(), new BlockPos(0, 0, 0)).createMenu(0, mockPlayer.getInventory(), mockPlayer);
        AtomicInteger total = new AtomicInteger();
        AtomicInteger correct = new AtomicInteger();
        RecipeCollection.INSTANCE.getRecipes().stream().filter(this::isCraftingTableRecipe).forEach(recipe -> {
            var parsedRecipe = parseCraftingTableRecipe(recipe);
            var desiredOutput = getDesiredOutPut(recipe);
            var actualOutput = craftRecipe(parsedRecipe, mockPlayer);

            if (desiredOutput.getItem() == actualOutput.getItem()) {
                total.getAndIncrement();
                correct.getAndIncrement();
            } else if (actualOutput.getItem() != Items.AIR) {
                Forgero.LOGGER.warn("expected {} to be output, but got {}. There is a possible conflict in the recipes", desiredOutput.toString(), actualOutput.toString());
                total.getAndIncrement();
            } else {
                total.getAndIncrement();
            }
        });

        if (total.get() == correct.get()) {
            Forgero.LOGGER.info("tested {} recipes, where {}/{} were correct", total.get(), correct.get(), total.get());
            mockPlayer.closeHandledScreen();
            context.complete();
        } else {
            Forgero.LOGGER.error("tested {} recipes, where {}/{} were correct", total.get(), correct.get(), total.get());
            throw new GameTestException("recipe testing failed");
        }
    }

    private boolean isCraftingTableRecipe(RecipeWrapper wrapper) {
        return switch (wrapper.getRecipeType()) {
            case HANDLE_RECIPE -> true;
            case BINDING_RECIPE -> true;
            case PICKAXEHEAD_RECIPE -> true;
            case SHOVELHEAD_RECIPE -> true;
            case TOOL_PART_SECONDARY_MATERIAL_UPGRADE -> false;
            case TOOL_RECIPE -> true;
            case TOOL_WITH_BINDING_RECIPE -> true;
        };
    }


    private Item testHandleRecipe(Item ingredient, ServerPlayerEntity player) {
        CraftingScreenHandler handler = ((CraftingScreenHandler) player.currentScreenHandler);
        handler.getSlot(3).setStack(new ItemStack(ingredient));
        handler.setStackInSlot(5, 1, new ItemStack(ingredient, 1));
        handler.setStackInSlot(7, 3, new ItemStack(ingredient));

        Item actualOutput = handler.getSlot(handler.getCraftingResultSlotIndex()).getStack().getItem();
        handler.clearCraftingSlots();
        return actualOutput;
    }

    private List<Pair<Integer, ItemStack>> parseCraftingTableRecipe(RecipeWrapper wrapper) {
        List<Pair<Integer, ItemStack>> ingredients = new ArrayList<>();
        JsonArray pattern = wrapper.getRecipe().getAsJsonArray("pattern");
        for (int i = 0; i < pattern.size(); i++) {
            String patternLine = wrapper.getRecipe().getAsJsonArray("pattern").get(i).getAsString();
            char emptyPattern = ' ';
            for (int j = 0; j < patternLine.length(); j++) {
                if (patternLine.charAt(j) != emptyPattern) {
                    String itemIdentifier = wrapper.getRecipe().getAsJsonObject("key").getAsJsonObject(String.valueOf(patternLine.charAt(j))).get("item").getAsString();
                    Identifier itemId = new Identifier(itemIdentifier);
                    ItemStack stack = new ItemStack(Registry.ITEM.get(itemId));
                    ingredients.add(new Pair<>((3 * i + j) + 1, stack));
                }
            }
        }
        return ingredients;
    }

    private ItemStack getDesiredOutPut(RecipeWrapper wrapper) {
        Identifier itemId = new Identifier(wrapper.getRecipe().getAsJsonObject("result").get("item").getAsString());
        return new ItemStack(Registry.ITEM.get(itemId));
    }

    private ItemStack craftRecipe(List<Pair<Integer, ItemStack>> ingredients, ServerPlayerEntity player) {
        CraftingScreenHandler handler = ((CraftingScreenHandler) player.currentScreenHandler);
        ingredients.forEach(ingredient -> handler.getSlot(ingredient.getLeft()).setStack(ingredient.getRight()));
        ItemStack actualOutput = handler.getSlot(handler.getCraftingResultSlotIndex()).getStack().copy();
        handler.clearCraftingSlots();
        return actualOutput;
    }


    private ServerPlayerEntity createDummyServerPlayer(TestContext context) {
        ServerPlayerEntity serverPlayer = new ServerPlayerEntity(context.getWorld().getServer(), context.getWorld(), new GameProfile(UUID.randomUUID(), "test-mock-serverPlayer"));
        serverPlayer.networkHandler = new ServerPlayNetworkHandler(context.getWorld().getServer(), new ClientConnection(NetworkSide.CLIENTBOUND), serverPlayer);
        return serverPlayer;
    }

    private ForgeroTool createDummyTool() {
        return ForgeroToolFactory.INSTANCE.createForgeroTool(createDummyToolPartHead(), createDummyToolPartHandle());
    }

    private ForgeroPickaxeItem createDummyToolItem() {
        ToolMaterialAdapter adapter = new ToolMaterialAdapter(createDummyDuoMaterial());
        ForgeroTool tool = createDummyTool();
        return new ForgeroPickaxeItem(adapter, 10, 10, new FabricItemSettings(), tool);
    }

    private ToolPartHead createDummyToolPartHead() {
        PrimaryMaterial material = new SimpleDuoMaterial(SimpleMaterialPOJO.createDefaultMaterialPOJO());
        return ForgeroToolPartFactory.INSTANCE.createToolPartHeadBuilder(material, ForgeroToolTypes.PICKAXE).createToolPart();
    }

    private ToolPartHandle createDummyToolPartHandle() {
        PrimaryMaterial material = new SimpleDuoMaterial(SimpleMaterialPOJO.createDefaultMaterialPOJO());
        return ForgeroToolPartFactory.INSTANCE.createToolPartHandleBuilder(material).createToolPart();
    }

    private SimpleDuoMaterial createDummyDuoMaterial() {
        return new SimpleDuoMaterial(SimpleMaterialPOJO.createDefaultMaterialPOJO());
    }
}
