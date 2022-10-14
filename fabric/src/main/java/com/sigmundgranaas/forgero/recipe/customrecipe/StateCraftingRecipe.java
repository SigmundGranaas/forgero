package com.sigmundgranaas.forgero.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.item.nbt.v2.CompositeEncoder;
import com.sigmundgranaas.forgero.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

public class StateCraftingRecipe extends ShapedRecipe {

    public StateCraftingRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getOutput());
    }

    @Override
    public ItemStack craft(CraftingInventory craftingInventory) {
        List<ItemStack> ingredients = new ArrayList<>();
        var target = ForgeroStateRegistry.STATES.get(Registry.ITEM.getId(this.getOutput().getItem()).toString());
        if (target.isPresent()) {
            var targetState = target.get();
            var builder = Composite.builder().type(targetState.type()).name(targetState.name()).nameSpace(targetState.nameSpace());
            for (int i = 0; i < craftingInventory.size(); i++) {
                var stack = craftingInventory.getStack(i);
                if (this.getIngredients().stream().filter(ingredient -> !ingredient.isEmpty()).anyMatch(ingredient -> ingredient.test(stack))) {
                    ingredients.add(craftingInventory.getStack(i));
                }
            }

            ingredients.stream().map(this::itemStackToState).flatMap(Optional::stream).forEach(builder::addIngredient);

            var finalState = builder.build();
            var nbt = new CompositeEncoder().encode(finalState);
            var output = getOutput().copy();
            output.getOrCreateNbt().put(FORGERO_IDENTIFIER, nbt);
            return output;
        }
        return getOutput().copy();
    }

    private Optional<State> itemStackToState(ItemStack stack) {
        Identifier id = Registry.ITEM.getId(stack.getItem());
        return ForgeroStateRegistry.STATES.get(id.toString());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return StateCraftingRecipeSerializer.INSTANCE;
    }

    public static class StateCraftingRecipeSerializer extends Serializer implements ForgeroRecipeSerializer {
        public static final StateCraftingRecipeSerializer INSTANCE = new StateCraftingRecipeSerializer();

        @Override
        public StateCraftingRecipe read(Identifier identifier, JsonObject jsonObject) {
            return new StateCraftingRecipe(super.read(identifier, jsonObject));
        }

        @Override
        public StateCraftingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new StateCraftingRecipe(super.read(identifier, packetByteBuf));
        }

        @Override
        public Identifier getIdentifier() {
            return new Identifier(ForgeroInitializer.MOD_NAMESPACE, RecipeTypes.STATE_CRAFTING_RECIPE.getName());
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return INSTANCE;
        }
    }
}