package com.sigmundgranaas.forgero.core.constructable;

import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.trinket.Trinket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Interface for all elements which can be upgraded with a non-essential resource
 */
public interface Upgradeable extends PropertyContainer {
    int getTotalUpgradeSlots();

    default List<UpgradeSlot<Upgrade>> getUpgradeSlots() {
        return new ArrayList<>();
    }

    default List<UpgradeSlot<Trinket>> getTrinketUpgradeSlot() {
        return new ArrayList<>();
    }

    default List<UpgradeSlot<SecondaryMaterial>> getMaterialSlots() {
        return new ArrayList<>();
    }

    default List<FilledUpgradeSlot<SecondaryMaterial>> getFilledSecondaryMaterialSlots() {
        return new ArrayList<>();
    }
}
