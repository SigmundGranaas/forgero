package com.sigmundgranaas.forgero.settings;

import com.google.gson.annotations.SerializedName;
import com.sigmundgranaas.forgero.Forgero;
import com.sigmundgranaas.forgero.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.resource.data.v2.data.DataResource;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder(toBuilder = true)
@Getter
public class ForgeroSettings {

    @Builder.Default
    @SerializedName("disabled_resources")
    private List<String> disabledResources = new ArrayList<>();

    @Builder.Default
    @SerializedName("disabled_packs")
    private List<String> disabledPacks = new ArrayList<>();


    public boolean filterResources(DataResource resource) {
        boolean filter = getDisabledResources().stream().noneMatch(disabled -> resource.identifier().equals(disabled));
        if (!filter) {
            Forgero.LOGGER.info("{} was disabled by user settings", resource.identifier());
        }
        return filter;
    }

    public boolean filterPacks(DataPackage dataPackage) {
        boolean filter = getDisabledPacks().stream().noneMatch(disabled -> dataPackage.identifier().equals(disabled));
        if (!filter) {
            Forgero.LOGGER.info("{} was disabled by user settings", dataPackage.identifier());
        }
        return filter;
    }

    public List<String> getDisabledResources() {
        return Objects.requireNonNullElse(disabledResources, new ArrayList<>());
    }

    public List<String> getDisabledPacks() {
        return Objects.requireNonNullElse(disabledPacks, new ArrayList<>());
    }
}
