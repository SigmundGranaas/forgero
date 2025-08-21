package com.sigmundgranaas.forgero.smithing.util;

import com.sigmundgranaas.forgero.core.type.Type;

public class TemperatureItemUtil {
    public static boolean shouldApplyTemperature(Type type) {
        if (type == null) {
            return false;
        }
        // Check for material type
        if (type.equals(Type.MATERIAL) || type.typeName().equals("MATERIAL")) {
            return true;
        }
        // Check for tool part type
        if (type.equals(Type.PART) || type.typeName().equals("TOOL_PART")) {
            return true;
        }
        // Recursively check parents for either condition
        for (Type parent : type.parent()) {
            if (shouldApplyTemperature(parent)) {
                return true;
            }
        }
        return false;
    }
}
