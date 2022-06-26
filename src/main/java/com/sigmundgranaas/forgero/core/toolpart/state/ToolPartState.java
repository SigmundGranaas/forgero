package com.sigmundgranaas.forgero.core.toolpart.state;

import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.material.material.SecondaryMaterial;
import com.sigmundgranaas.forgero.core.property.Property;
import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.Target;
import com.sigmundgranaas.forgero.core.property.attribute.ToolPartTarget;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPartTypes;
import com.sigmundgranaas.forgero.core.trinket.Trinket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Interface for
 */
public interface ToolPartState extends PropertyContainer {
    PrimaryMaterial getPrimarySlot();

    int getTotalUpgradeSlots();

    default Collection<UpgradeSlot<ToolPartUpgrade>> getUpgradeSlots() {
        return new ArrayList<>();
    }

    default Collection<UpgradeSlot<Trinket>> getTrinketUpgradeSlot() {
        return new ArrayList<>();
    }

    default Collection<UpgradeSlot<SecondaryMaterial>> getSecondaryMaterialSlots() {
        return new ArrayList<>();
    }

    ForgeroToolPartTypes getType();

    @Override
    default @NotNull List<Property> applyProperty(Target target) {
        var combinedTarget = target.combineTarget(ToolPartTarget.of(getType()));
        return getPropertiesByTarget(combinedTarget);
    }

    @Override
    @NotNull
    default List<Property> getRootProperties() {
        return getUpgradeSlots()
                .stream()
                .map(PropertyContainer::getRootProperties)
                .flatMap(Collection::stream)
                .toList();
    }

    private List<Property> getPropertiesByTarget(Target target) {
        return getUpgradeSlots()
                .stream()
                .map(slot -> slot.applyProperty(target))
                .flatMap(Collection::stream)
                .toList();
    }

    @Override
    @NotNull
    default List<Property> getProperties() {
        return getPropertiesByTarget(ToolPartTarget.of(getType()));
    }
}