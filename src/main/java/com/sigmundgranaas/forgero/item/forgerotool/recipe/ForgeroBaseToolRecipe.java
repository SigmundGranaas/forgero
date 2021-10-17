package com.sigmundgranaas.forgero.item.forgerotool.recipe;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.tool.instance.ForgeroToolCreator;
import com.sigmundgranaas.forgero.item.forgerotool.tool.instance.ForgeroToolInstance;
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

import java.util.Optional;

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

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        for (int i = 0; i < inventory.size(); ++i) {
            ItemStack currentItems = inventory.getStack(i);
            if (head.test(inventory.getStack(i + 3)) && handle.test(currentItems)) {
                return true;
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
            if (head.test(inventory.getStack(i + 3)) && handle.test(currentItems)) {
                headItem = inventory.getStack(i + 3);
                handleItem = inventory.getStack(i);
            }
        }
        assert headItem != null;
        assert handleItem != null;
        Optional<ForgeroToolInstance> forgeroToolInstance = ForgeroToolCreator.createForgeroToolInstance(itemOutput, headItem, handleItem);
        if (forgeroToolInstance.isPresent()) {
            ItemStack forgeroToolInstanceStack = new ItemStack((Item) forgeroToolInstance.get().getBaseItem());
            forgeroToolInstanceStack.setNbt(forgeroToolInstance.get().writeNbt());
            return forgeroToolInstanceStack;
        } else {
            LOGGER.debug("Unable to craft custom ForgeroToolInstance, returned default output");
            return new ItemStack(itemOutput);
        }
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