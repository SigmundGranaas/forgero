package com.sigmundgranaas.forgero.resource.data.v2.data;

import lombok.Builder;

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
    private List<Integer> offset = Collections.emptyList();

    public List<Integer> getOffset() {
        return Objects.requireNonNullElse(offset, Collections.emptyList());

    }

    public List<String> getTarget() {
        return target;
    }

    public String getTemplate() {
        return template;
    }
}
