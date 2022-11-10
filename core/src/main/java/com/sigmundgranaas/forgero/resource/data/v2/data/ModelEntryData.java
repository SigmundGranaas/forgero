package com.sigmundgranaas.forgero.resource.data.v2.data;

import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

@Builder
public class ModelEntryData {
    private List<String> target;
    private String template;
    @Builder.Default
    @Nullable
    private List<Integer> offset = Collections.emptyList();

    public List<Integer> getOffset() {
        return Objects.requireNonNullElse(offset, Collections.emptyList());

    }
    @NotNull
    public List<String> getTarget() {
        return target;
    }

    @NotNull
    public String getTemplate() {
        return Objects.requireNonNullElse(template, EMPTY_IDENTIFIER);
    }
}
