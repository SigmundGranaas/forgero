package com.sigmundgranaas.forgero.item.forgerotool.toolpart;

import com.sigmundgranaas.forgero.item.forgerotool.Modifier.ModifierConstants;
import com.sigmundgranaas.forgero.item.forgerotool.material.ForgeroMaterial;

import java.util.ArrayList;
import java.util.List;

public class ForgeroToolHead extends AbstractToolPart {

    public ForgeroToolHead(ForgeroMaterial primaryMaterial, ForgeroMaterial secondaryMaterial) {
        super(primaryMaterial, secondaryMaterial);
    }

    @Override
    public ArrayList<ModifierConstants> getPossibleModifiers() {
        return new ArrayList<>(List.of(ModifierConstants.MINING_SPEED_MODIFIER,
                ModifierConstants.DURABILITY_MODIFIER,
                ModifierConstants.ATTACK_SPEED_MODIFIER,
                ModifierConstants.ATTACK_DAMAGE_MODIFIER));
    }

    @Override
    public float calculateModifiers() {
        return 0;
    }
}
