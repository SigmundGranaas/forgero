package com.sigmundgranaas.forgero.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.material.material.EmptySecondaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.core.toolpart.factory.ToolPartBuilder;
import com.sigmundgranaas.forgero.item.NBTFactory;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolPartAdapter;
import com.sigmundgranaas.forgero.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.recipe.implementation.SmithingRecipeGetters;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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
        //String additionMaterialIdentifier = addition.toJson().getAsJsonObject().getAsJsonPrimitive("item").getAsString();
        ItemStack addition = inventory.getStack(1);
        SecondaryMaterial secondaryMaterial = ForgeroRegistry.MATERIAL.getSecondaryMaterials().stream().filter(material -> {
            if (material.getIngredient().tag == null) {
                return material.getIngredient().item.equals(Registry.ITEM.getId(addition.getItem()).toString());
            } else {
                try {
                    return addition.isIn(ServerTagManagerHolder.getTagManager().getTag(Registry.ITEM_KEY, new Identifier(material.getIngredient().tag), (tag) -> new Exception()));
                } catch (Exception e) {
                    return false;
                }
            }
        }).findFirst().orElse(new EmptySecondaryMaterial());
        for (int i = 0; i < inventory.size(); i++) {
            if (base.test(inventory.getStack(i))) {
                toolPartStack = inventory.getStack(i);
            }
        }

        assert toolPartStack != null;

        ForgeroToolPart toolPart = FabricToForgeroToolPartAdapter.createAdapter().getToolPart(toolPartStack).orElse(((ToolPartItem) toolPartStack.getItem()).getPart());


        ToolPartBuilder builder = ForgeroToolPartFactory.INSTANCE.createToolPartBuilderFromToolPart(toolPart).setSecondary(secondaryMaterial);

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
