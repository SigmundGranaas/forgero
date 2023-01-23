package com.sigmundgranaas.forgero.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.core.Forgero;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

public class ForgeroConfigurationLoader {
	public static final ForgeroConfiguration defaultConfiguration = new ForgeroConfiguration();

	public static String configurationFileName = "forgero_settings.json";
	public static String configurationFolderName = "config";
	public static Path configurationFilePath = Path.of(MessageFormat.format("./{0}/{1}", configurationFolderName, configurationFileName));

	public static ForgeroConfiguration configuration;

	public static ForgeroConfiguration load() {
		if (!Files.exists(configurationFilePath)) {
			configuration = createConfigurationFile();
			return configuration;
		}

		try (InputStream stream = Files.newInputStream(configurationFilePath)) {
			var gson = createGson();
			configuration = gson.fromJson(new JsonReader(new BufferedReader(new InputStreamReader(stream))), ForgeroConfiguration.class);

			if (configuration.resourceLogging) {
				Forgero.LOGGER.info("Loaded Forgero configuration, located at: {}", configurationFilePath);
			}

			return configuration;
		} catch (IOException e) {
			Forgero.LOGGER.warn("Unable to read Forgero configuration file, located at {}. Loading default configuration. Check if the formatting is correct. See stack trace below:", configurationFilePath);
			e.printStackTrace();

			// Worst case scenario, return the default configuration
			configuration = defaultConfiguration;
			return defaultConfiguration;
		}
	}

	public static void save() {
		try (FileWriter writer = new FileWriter(configurationFilePath.toString())) {
			var gson = createGson();
			var json = gson.toJson(configuration);
			writer.write(json);
		} catch (IOException e) {
			Forgero.LOGGER.warn("Unable to save Forgero configuration file, located at {}. See stack trace below:", configurationFilePath);
			e.printStackTrace();
		}
	}

	private static @NotNull Gson createGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		return gsonBuilder.setPrettyPrinting().create();
	}

	private static ForgeroConfiguration createConfigurationFile() {
		if (!new File(configurationFolderName).exists()) {
			Forgero.LOGGER.warn("Unable to create Forgero configuration file at {}. Configuration folder (/config) doesn't exist. Loading default configuration.", configurationFilePath);

			// Worst case scenario, return the default configuration
			return ForgeroConfigurationLoader.defaultConfiguration;
		}

		try (FileWriter writer = new FileWriter(configurationFilePath.toString())) {
			var gson = createGson();
			var json = gson.toJson(defaultConfiguration);
			writer.write(json);

			Forgero.LOGGER.info("Created Forgero configuration file, located at {}", configurationFilePath);

			return defaultConfiguration;
		} catch (IOException e) {
			Forgero.LOGGER.warn("Unable to create Forgero configuration file at {}. Loading default configuration. See stack trace below:", configurationFilePath);
			e.printStackTrace();

			// Worst case scenario, return the default configuration
			return ForgeroConfigurationLoader.defaultConfiguration;
		}
	}
}
