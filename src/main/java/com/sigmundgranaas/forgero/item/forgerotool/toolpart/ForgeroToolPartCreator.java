package com.sigmundgranaas.forgero.item.forgerotool.toolpart;

import com.sigmundgranaas.forgero.item.forgerotool.Modifier.EmptyModifier;
import com.sigmundgranaas.forgero.item.forgerotool.Modifier.ForgeroModifier;
import com.sigmundgranaas.forgero.item.forgerotool.material.ForgeroMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ForgeroToolPartCreator {
    public static Optional<ForgeroToolPart> createToolPart(NbtCompound nbtCompound) {
        String primaryMaterialIdentifier = nbtCompound.getString(ForgeroToolPart.PRIMARY_MATERIAL_IDENTIFIER);

        @Nullable
        ToolMaterial primaryMaterial = ForgeroMaterial.getMaterialMap().get(primaryMaterialIdentifier);
        if (primaryMaterial == null){
            return Optional.empty();
        }

        @Nullable
        ToolMaterial secondaryMaterial = ForgeroMaterial.getMaterialMap().get(nbtCompound.getString(ForgeroToolPart.SECONDARY_MATERIAL_IDENTIFIER));
        if(secondaryMaterial == null){
            secondaryMaterial = ForgeroMaterial.EMPTY_MATERIAL;
        }

        ForgeroModifier modifier = new EmptyModifier();


        return Optional.of(new ForgeroToolPart(primaryMaterial, secondaryMaterial, modifier));
    }

    public static ForgeroToolPart createToolPart(ItemStack forgeroToolPart) {
        return null;
    }

    public static ForgeroToolPart createToolPart(ForgeroToolPartItem forgeroToolPart) {
        return new ForgeroToolPart(forgeroToolPart.getMaterial(), ForgeroMaterial.EMPTY_MATERIAL, new EmptyModifier());
    }

    public static ForgeroToolPart createToolPart(Item forgeroToolPart) {
        return null;
    }

    private ForgeroToolPart createBasicToolPart() {
        return null;
    }
}
