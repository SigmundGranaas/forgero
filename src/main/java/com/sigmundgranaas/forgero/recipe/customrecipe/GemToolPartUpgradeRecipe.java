package com.sigmundgranaas.forgero.recipe.customrecipe;

import com.google.gson.JsonObject;
import com.sigmundgranaas.forgero.ForgeroInitializer;
import com.sigmundgranaas.forgero.core.gem.EmptyGem;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.core.toolpart.factory.ForgeroToolPartFactory;
import com.sigmundgranaas.forgero.core.toolpart.factory.ToolPartBuilder;
import com.sigmundgranaas.forgero.item.NBTFactory;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroGemAdapter;
import com.sigmundgranaas.forgero.item.adapter.FabricToForgeroToolPartAdapter;
import com.sigmundgranaas.forgero.item.implementation.NBTFactoryImpl;
import com.sigmundgranaas.forgero.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.recipe.implementation.SmithingRecipeGetters;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class GemToolPartUpgradeRecipe extends SmithingRecipe {

    public GemToolPartUpgradeRecipe(SmithingRecipeGetters recipe) {
        super(recipe.getId(), recipe.getBase(), recipe.getAddition(), recipe.getResult().copy());
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        if (super.matches(inventory, world)) {
            NbtCompound toolNbt = inventory.getStack(0).getOrCreateNbt();
            String gemType = toolNbt.getCompound(getToolPartType(toolNbt)).getString(NBTFactory.GEM_NBT_IDENTIFIER);
            return gemType.equals("") || gemType.equals(NBTFactoryImpl.createGemNbtString(EmptyGem.createEmptyGem()));
        }
        return false;
    }

    String getToolPartType(NbtCompound compound) {
        if (compound.contains(NBTFactory.HEAD_NBT_IDENTIFIER)) {
            return NBTFactory.HEAD_NBT_IDENTIFIER;
        } else if (compound.contains(NBTFactory.HANDLE_NBT_IDENTIFIER)) {
            return NBTFactory.HANDLE_NBT_IDENTIFIER;
        } else {
            return NBTFactory.BINDING_NBT_IDENTIFIER;
        }
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        Gem gem = FabricToForgeroGemAdapter.createAdapter().getGem(inventory.getStack(1)).orElse(EmptyGem.createEmptyGem());
        ItemStack toolPartStack = inventory.getStack(0);

        ForgeroToolPart toolpart = FabricToForgeroToolPartAdapter.createAdapter().getToolPart(toolPartStack).orElse(((ToolPartItem) toolPartStack.getItem()).getPart());

        ToolPartBuilder builder = ForgeroToolPartFactory.INSTANCE.createToolPartBuilderFromToolPart(toolpart).setGem(gem);

        ItemStack result = super.craft(inventory);
        result.getOrCreateNbt().put(NBTFactory.getToolPartNBTIdentifier(toolpart), NBTFactory.INSTANCE.createNBTFromToolPart(builder.createToolPart()));
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
            return new GemToolPartUpgradeRecipe((SmithingRecipeGetters) super.read(identifier, jsonObject));
        }

        @Override
        public SmithingRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            return new GemToolPartUpgradeRecipe((SmithingRecipeGetters) super.read(identifier, packetByteBuf));
        }

        @Override
        public Identifier getIdentifier() {
            return new Identifier(ForgeroInitializer.MOD_NAMESPACE, RecipeTypes.TOOL_PART_GEM_UPGRADE.getName());
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return INSTANCE;
        }
    }
}
