package com.sigmundgranaas.forgero.resource.data.v2.data;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;

import static com.sigmundgranaas.forgero.util.Identifiers.*;

@Builder
public class HostData {
    @Nullable
    @Builder.Default
    private String type = CREATE_IDENTIFIER;
    @Nullable
    @Builder.Default
    private String id = THIS_IDENTIFIER;

    @NotNull
    public String getType() {
        return Objects.requireNonNullElse(type, CREATE_IDENTIFIER);
    }

    @NotNull
    public String getId() {
        return Objects.requireNonNullElse(id, EMPTY_IDENTIFIER);
    }
}
