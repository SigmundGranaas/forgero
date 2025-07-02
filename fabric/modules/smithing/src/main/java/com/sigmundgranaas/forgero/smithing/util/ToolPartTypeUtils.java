package com.sigmundgranaas.forgero.smithing.util;

import com.sigmundgranaas.forgero.core.type.Type;

public class ToolPartTypeUtils {
    public static boolean isToolPartHeadOrToolPart(Type type) {
        if (type.equals(Type.TOOL_PART_HEAD) || type.typeName().equals("TOOL_PART")) {
            return true;
        }
        for (Type parent : type.parent()) {
            if (isToolPartHeadOrToolPart(parent)) {
                return true;
            }
        }
        return false;
    }
}

