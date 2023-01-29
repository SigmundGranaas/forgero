package com.sigmundgranaas.forgero.fabric.registry;

import com.sigmundgranaas.forgero.core.soul.PropertyLevelProvider;
import net.minecraft.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class DefaultLevelProperties {
    public static Map<String, PropertyLevelProvider> defaults() {
        Map<String, PropertyLevelProvider> map = new HashMap<>();
        //Put values I cannot convert to datafiles here
        return map;
    }

    public static String entityId(EntityType<?> type) {
        return EntityType.getId(type).toString();
    }
}


