package com.sigmundgranaas.forgero.item.forgerotool.tool.instance;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.tool.item.ForgeroTool;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartCreator;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartItem;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Class for creating forgeroTools from various sources
 */
public class ForgeroToolCreator {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);
    public static final String FORGERO_TOOL_IDENTIFIER = "FORGERO_TOOL";

    /**
     * Usable when creating a forgeroTool from an existing itemstack
     *
     * @param forgeroTool
     * @return Optional ForgeroToolInstance Will return if all existing information exists inside the itemstacks nbt
     */
    public static @NotNull Optional<ForgeroToolInstance> createForgeroToolInstance(@Nullable ItemStack forgeroTool) {
        if (forgeroTool == null) {
            LOGGER.warn("Cannot create Tool, as the ItemStack is null");
            return Optional.empty();
        }
        Item baseItem = forgeroTool.getItem();

        @Nullable
        NbtCompound itemStackCompound = forgeroTool.getNbt();
        if (!(baseItem instanceof ForgeroTool) || itemStackCompound == null || itemStackCompound.isEmpty()) {
            LOGGER.warn("Cannot create Tool, as the ItemStack is not a ForgeroTool, the NbtCompound is null or it is empty, returning empty");
            return Optional.empty();
        }

        Optional<ForgeroToolPart> toolPartHead = ForgeroToolPartCreator.createToolPart(itemStackCompound.getCompound(ForgeroToolPartCreator.TOOL_PART_HEAD_IDENTIFIER));
        Optional<ForgeroToolPart> toolPartHandle = ForgeroToolPartCreator.createToolPart(itemStackCompound.getCompound(ForgeroToolPartCreator.TOOL_PART_HANDLE_IDENTIFIER));
        if (toolPartHead.isEmpty() || toolPartHandle.isEmpty()) {
            LOGGER.warn("Cannot create Tool as the head or the handle is empty: Tool head present: {}, Tool handle present: {}", toolPartHead.isPresent(), toolPartHandle.isPresent());
            LOGGER.warn(itemStackCompound.getKeys().toString());
            return Optional.empty();
        }


        return switch (((ForgeroTool) baseItem).getToolHead().getToolPartType()) {
            case PICKAXE_HEAD -> Optional.of(new ForgeroPickaxeInstance((ForgeroTool) forgeroTool.getItem(), forgeroTool.getOrCreateNbt(), toolPartHead.get(), toolPartHandle.get(), null));
            default -> Optional.empty();
        };
    }

    public static @NotNull Optional<ForgeroToolInstance> createForgeroToolInstance(@Nullable NbtCompound itemStackCompound, Item baseItem) {
        NbtCompound forgeroToolCompound = itemStackCompound;
        if (forgeroToolCompound.contains(FORGERO_TOOL_IDENTIFIER)) {
            forgeroToolCompound = itemStackCompound.getCompound(FORGERO_TOOL_IDENTIFIER);
        }
        if (!(baseItem instanceof ForgeroTool) || forgeroToolCompound == null || forgeroToolCompound.isEmpty()) {
            LOGGER.warn("Cannot create Tool, as the ItemStack is not a ForgeroTool, the NbtCompound is null or empty, returning empty");
            return Optional.empty();
        }

        Optional<ForgeroToolPart> toolPartHead = ForgeroToolPartCreator.createToolPart(forgeroToolCompound.getCompound(ForgeroToolPartCreator.TOOL_PART_HEAD_IDENTIFIER));
        Optional<ForgeroToolPart> toolPartHandle = ForgeroToolPartCreator.createToolPart(forgeroToolCompound.getCompound(ForgeroToolPartCreator.TOOL_PART_HANDLE_IDENTIFIER));
        if (toolPartHead.isEmpty() || toolPartHandle.isEmpty()) {
            LOGGER.warn("Cannot create Tool as the head or the handle is empty: Tool head present: {}, Tool handle present: {}", toolPartHead.isPresent(), toolPartHandle.isPresent());
            LOGGER.warn(forgeroToolCompound.getKeys().toString());
            return Optional.empty();
        }

        return switch (((ForgeroTool) baseItem).getToolHead().getToolPartType()) {
            case PICKAXE_HEAD -> Optional.of(new ForgeroPickaxeInstance((ForgeroTool) baseItem, new NbtCompound(), toolPartHead.get(), toolPartHandle.get(), null));
            default -> Optional.empty();
        };
    }

    public static @NotNull Optional<ForgeroToolInstance> createForgeroToolInstance(Item forgeroItem, ItemStack head, ItemStack handle) {
        Item baseItemHead = head.getItem();
        Optional<ForgeroToolPart> toolPartHead = ForgeroToolPartCreator.createToolPart(head);
        if (!(baseItemHead instanceof ForgeroToolPartItem) || toolPartHead.isEmpty()) {
            LOGGER.warn("Cannot create Tool, as the ItemStack is not a ForgeroTool or the NbtCompound is null, returning empty");
            return Optional.empty();
        }
        Item baseItemHandle = handle.getItem();
        Optional<ForgeroToolPart> toolPartHandle = ForgeroToolPartCreator.createToolPart(handle);
        if (!(baseItemHandle instanceof ForgeroToolPartItem) || toolPartHandle.isEmpty()) {
            return Optional.empty();
        }
        ForgeroToolPartTypes headType = ((ForgeroToolPartItem) baseItemHead).getToolPartType();
        return switch (headType) {
            case PICKAXE_HEAD -> Optional.of(new ForgeroPickaxeInstance((ForgeroTool) forgeroItem, new NbtCompound(), toolPartHead.get(), toolPartHandle.get(), null));
            default -> Optional.empty();
        };
    }

    public static @Nullable Optional<ForgeroToolInstance> createForgeroToolInstance(ItemStack head, ItemStack handle, ItemStack binding) {
        return null;
    }

    private @Nullable ForgeroToolInstance createBasicForgeroToolInstance(ItemStack forgeroTool) {
        return null;
    }

}
