package com.sigmundgranaas.forgero.item.forgerotool.toolpart;

import com.sigmundgranaas.forgero.item.forgerotool.Modifier.ForgeroModifier;
import com.sigmundgranaas.forgero.item.forgerotool.Modifier.ModifierConstants;
import com.sigmundgranaas.forgero.item.forgerotool.material.ForgeroMaterial;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;

public interface ForgeroToolPart {
    ForgeroModifier getToolPartModifier(ArrayList<ModifierConstants> possibleModifiers);

    ArrayList<ModifierConstants> getPossibleModifiers();

    public NbtCompound getToolPartNbt();

    public ForgeroMaterial getPrimaryMaterial();

    public ForgeroMaterial getSecondaryMaterial();

    public NbtCompound createToolPartNbt();

    public float calculateModifiers();
}
