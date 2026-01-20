package com.sigmundgranaas.forgero.core.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.core.Forgero;
import org.jetbrains.annotations.NotNull;

public class ForgeroConfigurationLoader {
	public static final ForgeroConfiguration defaultConfiguration = new ForgeroConfiguration();

	public static String configurationFileName = "forgero_settings.json";
	public static Path configurationFolderPath;
	public static Path configurationFilePath;
	public static boolean havePathsBeenResolved = false;

	public static ForgeroConfiguration configuration = defaultConfiguration;

	public static ForgeroConfiguration load(Path configDir) {
		configurationFolderPath = configDir;
		configurationFilePath = configurationFolderPath.resolve(configurationFileName);
		havePathsBeenResolved = true;

		if (!Files.exists(configurationFilePath)) {
			configuration = createConfigurationFile();
			return configuration;
		}

		try (InputStream stream = Files.newInputStream(configurationFilePath)) {
			var gson = createGson();
			configuration = gson.fromJson(new JsonReader(new BufferedReader(new InputStreamReader(stream))), ForgeroConfiguration.class);
			save();
			if (configuration.resourceLogging) {
				Forgero.LOGGER.info("Loaded Forgero configuration, located at: {}", configurationFilePath);
			}

			return configuration;
		} catch (IOException e) {
			Forgero.LOGGER.warn(
					"Unable to read Forgero configuration file, located at {}. Loading default configuration. Check if the formatting is correct. See stack trace below:",
					configurationFilePath
			);
			e.printStackTrace();

			// Worst case scenario, return the default configuration
			configuration = defaultConfiguration;
			return defaultConfiguration;
		}
	}

	public static void save() {
		if (!havePathsBeenResolved) {
			Forgero.LOGGER.warn("Unable to save Forgero configuration file, configuration file path hasn't been resolved yet. ForgeroConfigurationLoader#load should be called before ForgeroConfigurationLoader#save.");
			return;
		}

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
		if (!havePathsBeenResolved) {
			Forgero.LOGGER.warn("Unable to create Forgero configuration file, configuration file path hasn't been resolved yet. ForgeroConfigurationLoader#load should be called before ForgeroConfigurationLoader#createConfigurationFile.");
			return ForgeroConfigurationLoader.defaultConfiguration;
		}

		if (!configurationFolderPath.toFile().exists()) {
			Forgero.LOGGER.warn(
					"Unable to create Forgero configuration file at {}. Configuration folder (/config) doesn't exist. Loading default configuration.",
					configurationFilePath
			);

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
			Forgero.LOGGER.warn(
					"Unable to create Forgero configuration file at {}. Loading default configuration. See stack trace below:",
					configurationFilePath
			);
			e.printStackTrace();

			// Worst case scenario, return the default configuration
			return ForgeroConfigurationLoader.defaultConfiguration;
		}
	}
}
