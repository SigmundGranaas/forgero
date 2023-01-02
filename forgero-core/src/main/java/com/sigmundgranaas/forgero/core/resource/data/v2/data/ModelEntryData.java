package com.sigmundgranaas.forgero.core.resource.data.v2.data;

import com.sigmundgranaas.forgero.core.util.Identifiers;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Builder
public class ModelEntryData {
    private List<String> target;
    private String template;
    @Builder.Default
    @Nullable
    private List<Float> offset = Collections.emptyList();

    public List<Float> getOffset() {
        return Objects.requireNonNullElse(offset, Collections.emptyList());

    }

    @NotNull
    public List<String> getTarget() {
        return target;
    }

    @NotNull
    public String getTemplate() {
        return Objects.requireNonNullElse(template, Identifiers.EMPTY_IDENTIFIER);
    }
}
