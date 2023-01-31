package com.sigmundgranaas.forgero.core.util;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public class Utils {
    @Nullable
    public static InputStream readJsonResourceAsString(String path) {
        return Utils.class.getResourceAsStream(path);
    }
}
