package com.sigmundgranaas.forgero.item.instance;

import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.tool.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.tool.ForgeroToolItem;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

public interface NBTFactory {
    NBTFactory INSTANCE = NBTFactoryImpl.getInstance();

    public static final String PRIMARY_MATERIAL_NBT_IDENTIFIER = "PrimaryMaterial";
    public static final String SECONDARY_MATERIAL_NBT_IDENTIFIER = "SecondaryMaterial";
    public static final String HEAD_NBT_IDENTIFIER = "Head";
    public static final String HANDLE_NBT_IDENTIFIER = "Handle";
    public static final String BINDING_NBT_IDENTIFIER = "Binding";
    public static final String FORGERO_TOOL_NBT_IDENTIFIER = "ForgeroTool";
    public static final String TOOL_PART_TYPE_NBT_IDENTIFIER = "ToolPartType";
    public static final String TOOL_PART_HEAD_TYPE_NBT_IDENTIFIER = "ToolPartHeadType";
    public static final String GEM_NBT_IDENTIFIER = "Gem";
    public static final String HASH_NBT_IDENTIFIER = "ToolHash";

    @NotNull
    ForgeroToolPart createToolPartFromNBT(@NotNull NbtCompound compound);


    @NotNull
    ForgeroTool createToolFromNBT(@NotNull ForgeroToolItem baseTool, @NotNull NbtCompound compound);


    @NotNull
    NbtCompound createNBTFromTool(@NotNull ForgeroTool baseTool);

    @NotNull
    NbtCompound createNBTFromToolPart(@NotNull ForgeroToolPart toolPart);
}
