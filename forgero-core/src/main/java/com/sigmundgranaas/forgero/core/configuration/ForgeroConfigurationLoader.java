package com.sigmundgranaas.forgero.core.configuration;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.resource.data.v2.DataPackage;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Set;

public class ForgeroConfigurationLoader {
	public static String configurationFileName = "forgero_settings.json";
	public static String configurationFolderName = "config";
	public static Path configurationFilePath = Path.of(MessageFormat.format("./{0}/{1}", configurationFolderName, configurationFileName));

	public static ForgeroConfiguration configuration;
	public static ForgeroConfiguration.Deserializer deserializer;

	public static ForgeroConfiguration load() {
		if (deserializer == null) deserializer = new ForgeroConfigurationData.Deserializer();

		try (InputStream stream = Files.newInputStream(configurationFilePath)) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(ForgeroConfiguration.class, deserializer);

			Gson configurationGson = gsonBuilder.create();
			configuration = configurationGson.fromJson(new JsonReader(new BufferedReader(new InputStreamReader(stream))), ForgeroConfiguration.class);

			if (configuration.resourceLogging) {
				Forgero.LOGGER.info("Reading Forgero configuration from: {}", configurationFilePath);
			}

			// TODO: Set configuration's available dependencies

			return configuration;
		} catch (Exception e) {
			Forgero.LOGGER.info("No Forgero configuration file detected in {}, creating a new configuration file...", configurationFilePath);
			return createConfigurationFile();
		}
	}

	// TODO: Call this in the right place
	public static boolean filterResources(DataResource resource) {
		boolean filter = ForgeroConfigurationLoader.configuration.disabledResources.stream().noneMatch(disabled -> resource.identifier().equals(disabled));
		if (!filter) {
			Forgero.LOGGER.info("{} was disabled by user settings", resource.identifier());
		}

		return filter;
	}

	// TODO: Call this in the right place
	public static boolean filterPacks(DataPackage dataPackage) {
		boolean filter = ForgeroConfigurationLoader.configuration.disabledPacks.stream().noneMatch(disabled -> dataPackage.identifier().equals(disabled));
		if (!filter) {
			Forgero.LOGGER.info("{} was disabled by user settings", dataPackage.identifier());
		}

		return filter;
	}

	public static ImmutableSet<String> getAvailableDependencies() {
		return ImmutableSet.<String>builder().add("forgero", "minecraft").build();
	}

	private static Gson createGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(ForgeroConfiguration.class, ForgeroConfigurationLoader.deserializer);

		return gsonBuilder.setPrettyPrinting().create();
	}

	private static ForgeroConfiguration createConfigurationFile() {
		try (FileWriter writer = new FileWriter(configurationFilePath.toString())) {
			var forgeroConfiguration = new ForgeroConfiguration();
			var json = createGson().toJson(forgeroConfiguration);
			writer.write(json);

			return forgeroConfiguration;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
