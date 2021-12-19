package com.sigmundgranaas.forgero.item.forgerotool.toolpart;

import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.item.forgerotool.Modifier.EmptyModifier;
import com.sigmundgranaas.forgero.item.forgerotool.Modifier.ForgeroModifier;
import com.sigmundgranaas.forgero.material.ForgeroToolMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ForgeroToolPartCreator {
    public static final Logger LOGGER = LogManager.getLogger(Forgero.MOD_NAMESPACE);
    public static String TOOL_PART_HEAD_IDENTIFIER = "TOOL_PART_HEAD";
    public static String TOOL_PART_HANDLE_IDENTIFIER = "TOOL_PART_HANDLE";
    public static String TOOL_PART_BINDING_IDENTIFIER = "TOOL_PART_BINDING";

    public static @NotNull
    Optional<ForgeroToolPart> createToolPart(@Nullable NbtCompound nbtCompound) {
        if (nbtCompound == null || nbtCompound.isEmpty()) {
            LOGGER.warn("Cannot create tool part, as the inputCompound is null or empty, returning empty toolPart");
            return Optional.empty();
        }

        String primaryMaterialIdentifier = nbtCompound.getString(ForgeroToolPart.PRIMARY_MATERIAL_IDENTIFIER);
        if (primaryMaterialIdentifier.equals("")) {
            LOGGER.warn("Cannot create toolpart as primaryMaterial was not found in NbtCompound using identifier {}", ForgeroToolPart.PRIMARY_MATERIAL_IDENTIFIER);
            return Optional.empty();
        }

        @Nullable
        ToolMaterial primaryMaterial = ForgeroToolMaterial.getMaterialMap().get(primaryMaterialIdentifier);
        if (primaryMaterial == null) {
            LOGGER.warn("Cannot create toolpart with Primary material: {} as it was not found in the materialMap", primaryMaterialIdentifier);
            return Optional.empty();
        }

        @Nullable
        ToolMaterial secondaryMaterial = ForgeroToolMaterial.getMaterialMap().get(nbtCompound.getString(ForgeroToolPart.SECONDARY_MATERIAL_IDENTIFIER));
        if (secondaryMaterial == null) {
            secondaryMaterial = ForgeroToolMaterial.EMPTY_MATERIAL;
        }

        ForgeroModifier modifier = new EmptyModifier();

        return Optional.of(new ForgeroToolPart(primaryMaterial, secondaryMaterial, modifier));
    }

    public static @NotNull
    Optional<ForgeroToolPart> createToolPart(ItemStack forgeroToolPart) {
        if (forgeroToolPart.getItem() instanceof ForgeroToolPartItem) {
            return Optional.of(new ForgeroToolPart(((ForgeroToolPartItem) forgeroToolPart.getItem()).getMaterial(), ForgeroToolMaterial.EMPTY_MATERIAL, new EmptyModifier()));
        } else {
            LOGGER.warn("Cannot create toolpart, as the Itemstack input is an ForgeroToolpart, returning empty");
            return Optional.empty();
        }
    }

    public static @NotNull
    ForgeroToolPart createToolPart(ForgeroToolPartItem forgeroToolPart) {
        return new ForgeroToolPart(forgeroToolPart.getMaterial(), ForgeroToolMaterial.EMPTY_MATERIAL, new EmptyModifier());
    }

    public static ForgeroToolPart createToolPart(Item forgeroToolPart) {
        return null;
    }

    private ForgeroToolPart createBasicToolPart() {
        return null;
    }
}
