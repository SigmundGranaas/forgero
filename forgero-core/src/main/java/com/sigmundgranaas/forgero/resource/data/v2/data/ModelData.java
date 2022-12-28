package com.sigmundgranaas.forgero.resource.data.v2.data;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.util.Identifiers;
import lombok.Builder;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Builder(toBuilder = true)
public class ModelData {
    @Builder.Default
    @Nullable
    private List<String> target = Collections.emptyList();

    @Builder.Default
    private int order = 0;

    @Builder.Default
    @SerializedName(value = "modelType", alternate = "type")
    @Nullable
    private String modelType = Identifiers.EMPTY_IDENTIFIER;
    @Builder.Default
    @Nullable
    private String template = Identifiers.EMPTY_IDENTIFIER;
    @Builder.Default
    @Nullable
    private String name = Identifiers.EMPTY_IDENTIFIER;
    @Builder.Default
    @Nullable
    private List<ModelEntryData> variants = Collections.emptyList();
    @Builder.Default
    @Nullable
    private List<Float> offset = Collections.emptyList();
    @Builder.Default
    @Nullable
    private String palette = Identifiers.EMPTY_IDENTIFIER;

    public List<String> getTarget() {
        return Objects.requireNonNullElse(target, Collections.emptyList());
    }

    public String getModelType() {
        return Objects.requireNonNullElse(modelType, Identifiers.EMPTY_IDENTIFIER);

    }

    public String getTemplate() {
        return Objects.requireNonNullElse(template, Identifiers.EMPTY_IDENTIFIER);

    }

    public String getName() {
        return Objects.requireNonNullElse(name, Identifiers.EMPTY_IDENTIFIER);

    }

    public int order() {
        return order;

    }

    public List<ModelEntryData> getVariants() {
        return Objects.requireNonNullElse(variants, Collections.emptyList());

    }

    public List<Float> getOffset() {
        return Objects.requireNonNullElse(offset, Collections.emptyList());

    }

    public String getPalette() {
        return Objects.requireNonNullElse(palette, Identifiers.EMPTY_IDENTIFIER);
    }
}
