package com.sigmundgranaas.forgerocommon.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgerocommon.conversion.StateConverter;
import com.sigmundgranaas.forgero.property.AttributeType;
import com.sigmundgranaas.forgerocommon.recipe.ForgeroRecipeSerializer;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.stream.IntStream;

public class RepairKitRecipe extends ShapelessRecipe {

    public RepairKitRecipe(ShapelessRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.getOutput(), recipe.getIngredients());
    }

    @Override
    public boolean matches(CraftingInventory craftingInventory, World world) {
        return super.matches(craftingInventory, world) && IntStream.range(0, 8)
                .mapToObj(craftingInventory::getStack)
                .filter(stack -> StateConverter.of(stack).isPresent())
                .anyMatch(stack -> stack.getDamage() > 0);
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory) {
        var state = IntStream.range(0, 8)
                .mapToObj(craftingInventory::getStack)
                .map(StateConverter::of)
                .flatMap(Optional::stream)
                .findFirst();
        var originalStack = IntStream.range(0, 8)
                .mapToObj(craftingInventory::getStack)
                .filter(stack -> StateConverter.of(stack).isPresent())
                .findFirst();
        if (state.isPresent() && originalStack.isPresent()) {
            int durability = (int) state.get().stream().applyAttribute(AttributeType.DURABILITY);

            var stack = originalStack.get();

            var newDamage = stack.getDamage() - (durability / 3);
            var newStack = stack.copy();
            newStack.setDamage(Math.max(newDamage, 0));
            return newStack;
        }

        return getOutput().copy();
    }


    @Override
    public RecipeSerializer<?> getSerializer() {
        return RepairKitRecipeSerializer.INSTANCE;
    }

    public static class RepairKitRecipeSerializer extends Serializer implements ForgeroRecipeSerializer {
        public static final RepairKitRecipeSerializer INSTANCE = new RepairKitRecipeSerializer();

        @Override
        public RecipeSerializer<?> getSerializer() {
            return INSTANCE;
        }

        @Override
        public RepairKitRecipe read(Identifier identifier, JsonObject jsonObject) {
            return new RepairKitRecipe(super.read(identifier, jsonObject));
        }

        @Override
        public RepairKitRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new RepairKitRecipe(super.read(identifier, packetByteBuf));
        }

        @Override
        public Identifier getIdentifier() {
            return new Identifier(Forgero.NAMESPACE, RecipeTypes.REPAIR_KIT_RECIPE.getName());
        }
    }
}