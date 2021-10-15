package com.sigmundgranaas.forgero.item.forgerotool.toolpart;

import com.sigmundgranaas.forgero.item.forgerotool.Modifier.ModifierConstants;
import com.sigmundgranaas.forgero.item.forgerotool.material.ForgeroMaterial;

import java.util.ArrayList;
import java.util.List;

public class ForgeroToolHandle extends AbstractToolPart {

    public ForgeroToolHandle(ForgeroMaterial primaryMaterial, ForgeroMaterial secondaryMaterial) {
        super(primaryMaterial, secondaryMaterial);
    }

    @Override
    public ArrayList<ModifierConstants> getPossibleModifiers() {
        return new ArrayList<>(List.of(ModifierConstants.MINING_SPEED_MODIFIER, ModifierConstants.ATTACK_SPEED_MODIFIER));
    }

    @Override
    public float calculateModifiers() {
        return 0;
    }
}
