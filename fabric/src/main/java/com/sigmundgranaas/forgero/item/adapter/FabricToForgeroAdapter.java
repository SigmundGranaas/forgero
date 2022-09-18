package com.sigmundgranaas.forgero.item.adapter;

import com.sigmundgranaas.forgero.tool.ForgeroTool;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.ForgeroToolItem;
import com.sigmundgranaas.forgero.item.NBTFactory;
import com.sigmundgranaas.forgero.item.ToolPartItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class FabricToForgeroAdapter implements FabricToForgeroToolAdapter, FabricToForgeroToolPartAdapter {
    @Override
    public boolean isTool(Identifier id) {
        return false;
    }

    @Override
    public boolean isTool(Item item) {
        return false;
    }

    @Override
    public boolean isTool(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean isTool(NbtCompound compound) {
        return false;
    }

    @Override
    public Optional<ForgeroTool> getTool(Identifier id) {
        return Optional.empty();
    }

    @Override
    public Optional<ForgeroTool> getTool(Item item) {
        return Optional.empty();
    }

    @Override
    public Optional<ForgeroTool> getTool(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ForgeroToolItem) {
            if (itemStack.getOrCreateNbt().contains(NBTFactory.FORGERO_TOOL_NBT_IDENTIFIER)) {
                assert itemStack.getNbt() != null;
                return Optional.of(NBTFactory.INSTANCE.createToolFromNBT((ForgeroToolItem) itemStack.getItem(), itemStack.getNbt()));
            } else {
                return Optional.of(getTool((ForgeroToolItem) itemStack.getItem()));
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<ForgeroTool> getTool(NbtCompound compound) {
        return Optional.empty();
    }

    @Override
    public ForgeroTool getTool(ForgeroToolItem toolItem) {
        return toolItem.getTool();
    }

    @Override
    public boolean isToolPart(Identifier id) {
        return false;
    }

    @Override
    public boolean isToolPart(Item item) {
        return false;
    }

    @Override
    public boolean isToolPart(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean isToolPart(NbtCompound compound) {
        return false;
    }

    @Override
    public Optional<ForgeroToolPart> getToolPart(Identifier id) {
        return Optional.empty();
    }

    @Override
    public Optional<ForgeroToolPart> getToolPart(Item item) {
        return Optional.empty();
    }

    @Override
    public Optional<ForgeroToolPart> getToolPart(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ToolPartItem) {
            if (itemStack.getOrCreateNbt().contains(NBTFactory.getToolPartNBTIdentifier(((ToolPartItem) itemStack.getItem()).getPart()))) {
                assert itemStack.getNbt() != null;
                return NBTFactory.INSTANCE.createToolPartFromNBT(itemStack.getNbt().getCompound(NBTFactory.getToolPartNBTIdentifier(((ToolPartItem) itemStack.getItem()).getPart())));
            } else {
                return Optional.of(((ToolPartItem) itemStack.getItem()).getPart());
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<ForgeroToolPart> getToolPart(NbtCompound compound) {
        return Optional.empty();
    }

    @Override
    public ForgeroToolPart getToolPart(ToolPartItem toolItem) {
        return null;
    }
}
