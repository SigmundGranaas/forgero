package com.sigmundgranaas.forgero.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.core.toolpart.factory.ToolPartBuilder;
import com.sigmundgranaas.forgero.item.NBTFactory;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.recipe.implementation.SmithingRecipeGetters;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;

public class SecondaryMaterialToolPartUpgradeRecipe extends SmithingRecipe {


    private final Ingredient base;
    private final Ingredient addition;

    public SecondaryMaterialToolPartUpgradeRecipe(SmithingRecipeGetters recipe) {
        super(recipe.getId(), recipe.getBase(), recipe.getAddition(), recipe.getResult().copy());
        Identifier id = recipe.getId();
        this.base = recipe.getBase();
        this.addition = recipe.getAddition();
        ItemStack output = recipe.getResult();
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        ItemStack toolPartStack = null;
        String additionMaterialIdentifier = addition.toJson().getAsJsonObject().getAsJsonPrimitive("item").getAsString();
        SecondaryMaterial secondaryMaterial = ForgeroRegistry.getInstance().materialCollection().getSecondaryMaterialsAsList().stream().filter(material -> material.getIngredient().equals(additionMaterialIdentifier)).findFirst().orElse(new EmptySecondaryMaterial());
        for (int i = 0; i < inventory.size(); i++) {
            if (base.test(inventory.getStack(i))) {
                toolPartStack = inventory.getStack(i);
            }
        }
        assert toolPartStack != null;

        ToolPartBuilder builder = ForgeroToolPartFactory.INSTANCE.createToolPartBuilderFromToolPart(((ToolPartItem) toolPartStack.getItem()).getPart()).setSecondary(secondaryMaterial);

        ItemStack result = super.craft(inventory);
        result.getOrCreateNbt().put(NBTFactory.getToolPartNBTIdentifier(((ToolPartItem) toolPartStack.getItem()).getPart()), NBTFactory.INSTANCE.createNBTFromToolPart(builder.createToolPart()));

        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer extends SmithingRecipe.Serializer implements ForgeroRecipeSerializer {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public SmithingRecipe read(Identifier identifier, JsonObject jsonObject) {
            return new SecondaryMaterialToolPartUpgradeRecipe((SmithingRecipeGetters) super.read(identifier, jsonObject));
        }

        @Override
        public SmithingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new SecondaryMaterialToolPartUpgradeRecipe((SmithingRecipeGetters) super.read(identifier, packetByteBuf));
        }

        @Override
        public Identifier getIdentifier() {
            return new Identifier(ForgeroInitializer.MOD_NAMESPACE, RecipeTypes.TOOL_PART_SECONDARY_MATERIAL_UPGRADE.getName());
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return INSTANCE;
        }
    }
}
