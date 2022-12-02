package com.sigmundgranaas.forgero.resource.data.v2.data;

import lombok.Builder;

import javax.annotation.Nullable;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

@Builder(toBuilder = true)
public class ContextData {
    @Builder.Default
    private final String fileName = EMPTY_IDENTIFIER;

    @Builder.Default
    private final String folder = EMPTY_IDENTIFIER;

    @Builder.Default
    private final String path = EMPTY_IDENTIFIER;

    @Nullable
    private final String parent;

    public String fileName() {
        return fileName;
    }

    public String folder() {
        return folder;
    }

    public String path() {
        return path;
    }

    public String parent() {
        return parent;
    }

}
