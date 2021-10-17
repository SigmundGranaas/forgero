package com.sigmundgranaas.forgero.item.forgerotool.tool.instance;

import com.sigmundgranaas.forgero.item.forgerotool.tool.ForgeroToolTypes;
import com.sigmundgranaas.forgero.item.forgerotool.tool.item.ForgeroTool;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.forgerotool.toolpart.ForgeroToolPartCreator;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ForgeroToolInstance {
    private final ForgeroTool baseItem;

    private final @NotNull NbtCompound rootNbt;

    private final @NotNull ForgeroToolPart head;

    private final @NotNull ForgeroToolPart handle;

    private final @Nullable ForgeroToolPart binding;

    public ForgeroToolInstance(@NotNull ForgeroTool forgeroTool, @NotNull NbtCompound rootNbt, @NotNull ForgeroToolPart head, @NotNull ForgeroToolPart handle, @Nullable ForgeroToolPart binding) {
        this.baseItem = forgeroTool;
        this.rootNbt = rootNbt;
        this.head = head;
        this.handle = handle;
        this.binding = binding;
    }

    public ForgeroTool getBaseItem() {
        return this.baseItem;
    }

    public NbtCompound getNbt() {
        return this.rootNbt;
    }


    public @NotNull NbtCompound getRootNbt() {
        return rootNbt;
    }

    public @NotNull ForgeroToolPart getHead() {
        return head;
    }

    public @NotNull ForgeroToolPart getHandle() {
        return handle;
    }

    public @Nullable ForgeroToolPart getBinding() {
        return binding;
    }

    public abstract ForgeroToolTypes getToolType();

    @NotNull
    public NbtCompound writeNbt() {
        NbtCompound forgeroToolNbt = new NbtCompound();
        forgeroToolNbt.putString("ToolType", getToolType().toString());
        forgeroToolNbt.put(ForgeroToolPartCreator.TOOL_PART_HEAD_IDENTIFIER, head.writeNbt());
        forgeroToolNbt.put(ForgeroToolPartCreator.TOOL_PART_HANDLE_IDENTIFIER, handle.writeNbt());
        if (binding != null) {
            forgeroToolNbt.put(ForgeroToolPartCreator.TOOL_PART_BINDING_IDENTIFIER, binding.writeNbt());
        }
        return forgeroToolNbt;
    }


}
