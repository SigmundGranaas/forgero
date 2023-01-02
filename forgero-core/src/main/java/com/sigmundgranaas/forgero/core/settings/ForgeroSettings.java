package com.sigmundgranaas.forgero.core.settings;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.DataPackage;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Builder(toBuilder = true)
@Getter
public class ForgeroSettings {

    public static Deserializer deserializer = new Deserializer();
    public static ForgeroSettings SETTINGS = SettingsLoader.load();
    @Builder.Default
    @NotNull
    @SerializedName("disabled_resources")
    private List<String> disabledResources = new ArrayList<>();

    @Builder.Default
    @NotNull
    @SerializedName("disabled_packs")
    private List<String> disabledPacks = new ArrayList<>();

    @Builder.Default
    @SerializedName("enable_repair_kits")
    private Boolean enableRepairKits = true;

    @Builder.Default
    @SerializedName("resource_logging")
    private Boolean resourceLogging = true;

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

    public static class Deserializer implements JsonDeserializer<ForgeroSettings> {
        @Override
        public ForgeroSettings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject data = json.getAsJsonObject();
            var builder = ForgeroSettings.builder();

            setIfNotNull(data.get("disabled_resources"), builder::disabledResources);
            setIfNotNull(data.get("disabled_packs"), builder::disabledPacks);
            setIfNotNull(data.get("enable_repair_kits"), builder::enableRepairKits);
            setIfNotNull(data.get("resource_logging"), builder::resourceLogging);
            return builder.build();
        }

        private <T> void setIfNotNull(JsonElement element, Consumer<T> setter) {
            if (element != null) {
                Gson gson = new Gson();
                TypeToken<T> typeToken = new TypeToken<T>() {
                };
                T parsed = gson.fromJson(element, typeToken.getType());
                if (parsed != null) {
                    setter.accept(parsed);
                }
            }
        }
    }
}
