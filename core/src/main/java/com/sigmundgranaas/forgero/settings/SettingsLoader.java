package com.sigmundgranaas.forgero.settings;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.Forgero;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsLoader {

    public static String settingsFileName = "forgero_settings.json";
    public static String settingsLocation = "./config/" + settingsFileName;


    public static ForgeroSettings load() {
        Path path = Path.of(settingsLocation);
        try (InputStream stream = Files.newInputStream(path)) {
            if (stream != null) {
                Forgero.LOGGER.info("Reading Forgero setting from:  {}", path);
                Gson settingsGson = new Gson();
                ForgeroSettings settings = settingsGson.fromJson(new JsonReader(new BufferedReader(new InputStreamReader(stream))), ForgeroSettings.class);
                return settings;
            }

        } catch (Exception e) {
            Forgero.LOGGER.info("No Forgero settings file detected in {}", path);
        }
        return ForgeroSettings.builder().build();
    }

}
