package com.sigmundgranaas.forgero.item;

import com.sigmundgranaas.forgero.core.data.v1.pojo.PropertyPOJO;
import com.sigmundgranaas.forgero.core.gem.Gem;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.tool.ForgeroTool;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import com.sigmundgranaas.forgero.item.implementation.NBTFactoryImpl;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface NBTFactory {
    NBTFactory INSTANCE = NBTFactoryImpl.getInstance();
    String PRIMARY_MATERIAL_NBT_IDENTIFIER = "PrimaryMaterial";
    String SECONDARY_MATERIAL_NBT_IDENTIFIER = "SecondaryMaterial";
    String SCHEMATIC_NBT_IDENTIFIER = "Schematic";
    String HEAD_NBT_IDENTIFIER = "Head";
    String HANDLE_NBT_IDENTIFIER = "Handle";
    String BINDING_NBT_IDENTIFIER = "Binding";
    String FORGERO_TOOL_NBT_IDENTIFIER = "ForgeroTool";
    String TOOL_PART_TYPE_NBT_IDENTIFIER = "ToolPartType";
    String TOOL_PART_HEAD_TYPE_NBT_IDENTIFIER = "ToolPartHeadType";
    String GEM_NBT_IDENTIFIER = "Gem";
    String HASH_NBT_IDENTIFIER = "ToolHash";
    String TOOL_PART_IDENTIFIER = "ToolPartIdentifier";

    String PROPERTY_IDENTIFIER = "Property";

    String ATTRIBUTES_IDENTIFIER = "Attributes";

    String ACTIVE_IDENTIFIER = "Active";

    String PASSIVE_IDENTIFIER = "Passive";

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

    @NotNull
    Optional<Schematic> createSchematicFromNbt(@NotNull NbtCompound compound);

    @NotNull
    List<Property> createPropertiesFromNbt(@NotNull NbtCompound compound);

    @NotNull
    NbtCompound createNbtFromProperties(@NotNull PropertyPOJO properties);

    @NotNull
    NbtCompound createNBTFromSchematic(@NotNull Schematic schematic);
}
