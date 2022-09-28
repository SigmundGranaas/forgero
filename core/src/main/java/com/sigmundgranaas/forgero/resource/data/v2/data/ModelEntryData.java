package com.sigmundgranaas.forgero.resource.data.v2.data;

import lombok.Builder;

import java.util.List;

@Builder
public class ModelEntryData {
    private List<String> target;
    private String template;
}
