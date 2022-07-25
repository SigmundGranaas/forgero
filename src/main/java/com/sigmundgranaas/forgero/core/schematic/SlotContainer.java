package com.sigmundgranaas.forgero.core.schematic;

import com.sigmundgranaas.forgero.core.constructable.*;
import com.sigmundgranaas.forgero.core.data.v1.pojo.SchematicPojo;

import java.util.List;

public record SlotContainer(
        List<DataSlot<Upgrade>> slots) {
    public static SlotContainer createContainer(List<SchematicPojo.SlotDataContainer> dataContainers){
        return new SlotContainer(dataContainers.stream().map(data -> new DataSlot<>(data.index, new SlotType(data.slotType), data.type)).toList());
    }

    public record DataSlot<T extends Upgrade>(int index, SlotType slotType,
                                              String placement) {
        public EmptyUpgradeSlot<T> toEmptyUpgradeSlot() {
            return new EmptyUpgradeSlot<T>(slotType, new SlotPlacement(placement, index));
        }
    }
}
