package com.sigmundgranaas.forgero.item.forgerotool.toolpart;

import com.sigmundgranaas.forgero.item.forgerotool.Modifier.ForgeroModifier;
import net.minecraft.item.ToolMaterial;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ForgeroToolPart {
    public static final String PRIMARY_MATERIAL_IDENTIFIER = "PrimaryMaterial";
    public static final String SECONDARY_MATERIAL_IDENTIFIER = "SecondaryMaterial";
    public static final String MODIFIER_IDENTIFIER = "ForgeroModifier";
    public static final String FORGERO_TOOL_PART_IDENTIFIER = "ForgeroToolPart";
    public static final String FORGERO_TOOL_PART_TYPE_IDENTIFIER = "ForgeroToolPartType";
    private final ToolMaterial primaryMaterial;
    @Nullable
    private final ToolMaterial secondaryMaterial;
    @Nullable
    private final ForgeroModifier modifier;

    public ForgeroToolPart(@NotNull ToolMaterial primaryMaterial, @NotNull ToolMaterial secondaryMaterial, @NotNull ForgeroModifier modifier) {
        this.primaryMaterial = primaryMaterial;
        this.secondaryMaterial = secondaryMaterial;
        this.modifier = modifier;
    }
}
