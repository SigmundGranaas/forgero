package com.sigmundgranaas.forgero.smithing.util;

import com.sigmundgranaas.forgero.core.type.Type;

public class ToolPartTypeUtils {
    public static boolean isToolPartType(Type type) {
        if (type == null) {
            return false;
        }
        if (type.equals(Type.PART) || type.typeName().equals("TOOL_PART")) {
            return true;
        }
        for (Type parent : type.parent()) {
            if (isToolPartType(parent)) {
                return true;
            }
        }
        return false;
    }
}
