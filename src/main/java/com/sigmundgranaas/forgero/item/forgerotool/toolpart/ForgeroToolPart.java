package com.sigmundgranaas.forgero.item.forgerotool.toolpart;

import com.sigmundgranaas.forgero.item.forgerotool.Modifier.ForgeroModifier;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

/**
 * ForgeroToolPart represents either a Head, handle or a binding of a ForgeroTool
 * This can be constructed from an NbtCombound and should be used for calculating appropriate attributes for a ForgeroTool
 */
public class ForgeroToolPart {
    public static final String PRIMARY_MATERIAL_IDENTIFIER = "PrimaryMaterial";
    public static final String SECONDARY_MATERIAL_IDENTIFIER = "SecondaryMaterial";
    public static final String MODIFIER_IDENTIFIER = "ForgeroModifier";
    public static final String FORGERO_TOOL_PART_IDENTIFIER = "ForgeroToolPart";
    public static final String FORGERO_TOOL_PART_TYPE_IDENTIFIER = "ForgeroToolPartType";
    @NotNull
    private final ToolMaterial primaryMaterial;
    @NotNull
    private final ToolMaterial secondaryMaterial;
    @NotNull
    private final ForgeroModifier modifier;

    public ForgeroToolPart(@NotNull ToolMaterial primaryMaterial, @NotNull ToolMaterial secondaryMaterial, @NotNull ForgeroModifier modifier) {
        this.primaryMaterial = primaryMaterial;
        this.secondaryMaterial = secondaryMaterial;
        this.modifier = modifier;
    }

    public @NotNull ToolMaterial getPrimaryMaterial() {
        return primaryMaterial;
    }

    public @NotNull ToolMaterial getSecondaryMaterial() {
        return secondaryMaterial;
    }

    public @NotNull ForgeroModifier getModifier() {
        return modifier;
    }

    public @NotNull NbtCompound writeNbt() {
        NbtCompound forgeroToolPartNbt = new NbtCompound();
        forgeroToolPartNbt.putString(PRIMARY_MATERIAL_IDENTIFIER, primaryMaterial.toString());
        forgeroToolPartNbt.putString(SECONDARY_MATERIAL_IDENTIFIER, secondaryMaterial.toString());
        forgeroToolPartNbt.putString(MODIFIER_IDENTIFIER, modifier.toString());
        return forgeroToolPartNbt;
    }
}
