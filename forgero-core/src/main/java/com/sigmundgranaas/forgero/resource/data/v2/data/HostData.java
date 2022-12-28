package com.sigmundgranaas.forgero.resource.data.v2.data;

import com.sigmundgranaas.forgero.util.Identifiers;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;

@Builder
public class HostData {
    @Nullable
    @Builder.Default
    private String type = Identifiers.CREATE_IDENTIFIER;
    @Nullable
    @Builder.Default
    private String id = Identifiers.THIS_IDENTIFIER;

    @NotNull
    public String getType() {
        return Objects.requireNonNullElse(type, Identifiers.CREATE_IDENTIFIER);
    }

    @NotNull
    public String getId() {
        return Objects.requireNonNullElse(id, Identifiers.EMPTY_IDENTIFIER);
    }
}
