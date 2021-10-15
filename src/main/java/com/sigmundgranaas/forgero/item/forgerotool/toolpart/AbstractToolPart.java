package com.sigmundgranaas.forgero.item.forgerotool.toolpart;

import com.sigmundgranaas.forgero.item.forgerotool.Modifier.EmptyModifier;
import com.sigmundgranaas.forgero.item.forgerotool.Modifier.ForgeroModifier;
import com.sigmundgranaas.forgero.item.forgerotool.Modifier.ModifierConstants;
import com.sigmundgranaas.forgero.item.forgerotool.material.ForgeroMaterial;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;


public abstract class AbstractToolPart implements ForgeroToolPart {
    private static final String PRIMARY_MATERIAL_IDENTIFIER = "PrimaryMaterial";
    private static final String SECONDARY_MATERIAL_IDENTIFIER = "SecondaryMaterial";
    private final ForgeroMaterial primaryMaterial;
    private final ForgeroMaterial secondaryMaterial;
    private NbtCompound toolPartNbt;
    private ForgeroModifier modifier;

    public AbstractToolPart(ForgeroMaterial primaryMaterial, ForgeroMaterial secondaryMaterial) {
        this.primaryMaterial = primaryMaterial;
        this.secondaryMaterial = secondaryMaterial;
        this.toolPartNbt = new NbtCompound();
    }

    public ForgeroModifier getModifier() {
        return modifier;
    }

    public void setModifier(ForgeroModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public ForgeroModifier getToolPartModifier(ArrayList<ModifierConstants> possibleModifiers) {
        for (ModifierConstants possibleModifier : possibleModifiers) {
            if (toolPartNbt.contains(possibleModifier.getNbtName())) {
                return ForgeroModifier.createForgeroModifier(possibleModifier, null);
            }
        }
        return new EmptyModifier();
    }

    @Override
    public NbtCompound createToolPartNbt() {
        NbtCompound newNbtComound = new NbtCompound();
        return newNbtComound;
    }

    @Override
    public NbtCompound getToolPartNbt() {
        return toolPartNbt;
    }

    public void setToolPartNbt(NbtCompound toolPartNbt) {
        this.toolPartNbt = toolPartNbt;
    }

    @Override
    public ForgeroMaterial getPrimaryMaterial() {
        return primaryMaterial;
    }

    @Override
    public ForgeroMaterial getSecondaryMaterial() {
        return secondaryMaterial;
    }
}
