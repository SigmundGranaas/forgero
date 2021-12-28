package com.sigmundgranaas.forgero.recipe;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.factory.ForgeroToolFactory;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHandle;
import com.sigmundgranaas.forgero.core.tool.toolpart.ToolPartHead;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.NBTFactory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ForgeroBaseToolRecipe implements CraftingRecipe {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);
    private final Ingredient handle;
    private final Ingredient head;
    private final Item itemOutput;
    private final Identifier id;

    public ForgeroBaseToolRecipe(Ingredient handle, Ingredient head, Item itemOutput, Identifier id) {
        this.handle = handle;
        this.head = head;
        this.itemOutput = itemOutput;
        this.id = id;
    }

    public Ingredient getHandle() {
        return handle;
    }

    public Item getItemOutput() {
        return itemOutput;
    }

    public Ingredient getHead() {
        return head;
    }

    private boolean containsAdditionalItems(CraftingInventory inventory) {
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack currentItems = inventory.getStack(i);
            if (!handle.test(currentItems) && !head.test(currentItems) && !currentItems.toString().equals("1 air")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        if (containsAdditionalItems(inventory)) {
            return false;
        }
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack currentItems = inventory.getStack(i);
            if (head.test(inventory.getStack(i + 2)) && handle.test(currentItems)) {
                return i != 0 && i != 3 && i != 6;
            }
        }
        return false;
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        @Nullable
        ItemStack headItem = null;
        @Nullable
        ItemStack handleItem = null;
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack currentItems = inventory.getStack(i);
            if (head.test(inventory.getStack(i + 2)) && handle.test(currentItems)) {
                handleItem = inventory.getStack(i + 2);
                headItem = inventory.getStack(i);
            }
        }
        assert headItem != null;
        assert handleItem != null;


        ToolPartHead head;
        ToolPartHandle handle;
        if (headItem.hasNbt() && headItem.getNbt().contains(NBTFactory.TOOL_PART_TYPE_NBT_IDENTIFIER)) {
            head = (ToolPartHead) NBTFactory.INSTANCE.createToolPartFromNBT(headItem.getNbt());
        } else {
            head = ((ForgeroToolItem) itemOutput).getHead();
        }

        if (handleItem.hasNbt() && handleItem.getNbt().contains(NBTFactory.TOOL_PART_TYPE_NBT_IDENTIFIER)) {
            handle = (ToolPartHandle) NBTFactory.INSTANCE.createToolPartFromNBT(handleItem.getNbt());
        } else {
            handle = ((ForgeroToolItem) itemOutput).getHandle();
        }

        ForgeroTool tool = ForgeroToolFactory.INSTANCE.createForgeroTool(head, handle);

        ItemStack forgeroToolInstanceStack = new ItemStack(itemOutput);
        forgeroToolInstanceStack.getOrCreateNbt().put(NBTFactory.FORGERO_TOOL_NBT_IDENTIFIER, NBTFactory.INSTANCE.createNBTFromTool(tool));
        return forgeroToolInstanceStack;
    }

    @Override
    public boolean fits(int width, int height) {
        return width * height >= 4;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return false;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public ItemStack getOutput() {
        return new ItemStack(this.itemOutput);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ForgeroBaseToolRecipeSerializer.INSTANCE;
    }
}