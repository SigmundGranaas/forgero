package com.sigmundgranaas.forgero.resource.data.v2.data;

import lombok.Builder;

import java.util.Collections;
import java.util.List;

import static com.sigmundgranaas.forgero.util.Identifiers.EMPTY_IDENTIFIER;

@Builder
public class ModelData {
    @Builder.Default
    private List<String> target = Collections.emptyList();
    @Builder.Default
    private String modelType = EMPTY_IDENTIFIER;
    private String template;
    private String name;
    private List<ModelEntryData> variants;
    private List<Integer> offset;
    private String palette;
}
