package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.implementation.NBTFactoryImpl;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

public interface NBTFactory {
    NBTFactory INSTANCE = NBTFactoryImpl.getInstance();
    String PRIMARY_MATERIAL_NBT_IDENTIFIER = "PrimaryMaterial";
    String SECONDARY_MATERIAL_NBT_IDENTIFIER = "SecondaryMaterial";
    String HEAD_NBT_IDENTIFIER = "Head";
    String HANDLE_NBT_IDENTIFIER = "Handle";
    String BINDING_NBT_IDENTIFIER = "Binding";
    String FORGERO_TOOL_NBT_IDENTIFIER = "ForgeroTool";
    String TOOL_PART_TYPE_NBT_IDENTIFIER = "ToolPartType";
    String TOOL_PART_HEAD_TYPE_NBT_IDENTIFIER = "ToolPartHeadType";
    String GEM_NBT_IDENTIFIER = "Gem";
    String HASH_NBT_IDENTIFIER = "ToolHash";
    String TOOL_PART_IDENTIFIER = "ToolPartIdentifier";

    static String getToolPartNBTIdentifier(ForgeroToolPart part) {
        return switch (part.getToolPartType()) {
            case HANDLE -> HANDLE_NBT_IDENTIFIER;
            case BINDING -> BINDING_NBT_IDENTIFIER;
            case HEAD -> HEAD_NBT_IDENTIFIER;
        };
    }

    @NotNull
    ForgeroToolPart createToolPartFromNBT(@NotNull NbtCompound compound);

    @NotNull
    ForgeroTool createToolFromNBT(@NotNull ForgeroToolItem baseTool, @NotNull NbtCompound compound);

    @NotNull
    NbtCompound createNBTFromTool(@NotNull ForgeroTool baseTool);

    @NotNull
    NbtCompound createNBTFromToolPart(@NotNull ForgeroToolPart toolPart);

    @NotNull
    Gem createGemFromNbt(@NotNull NbtCompound compound);

    @NotNull
    NbtCompound createNBTFromGem(@NotNull Gem gem, NbtCompound compound);
}
