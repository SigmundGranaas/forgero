package com.sigmundgranaas.forgero.core.resource.data.v2.data;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder(toBuilder = true)
public class PaletteData {
    private String name;
    private String type;
    @Builder.Default
    private List<String> include = new ArrayList<>();
    @Builder.Default
    private List<String> exclude = new ArrayList<>();

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<String> getInclude() {
        return include;
    }

    public List<String> getExclude() {
        return exclude;
    }
}
