package com.sigmundgranaas.forgero.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.core.Forgero;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;

public class ForgeroConfigurationLoader {
	public static String configurationFileName = "forgero_settings.json";
	public static String configurationFolderName = "config";
	public static Path configurationFilePath = Path.of(MessageFormat.format("./{0}/{1}", configurationFolderName, configurationFileName));

	public static ForgeroConfiguration configuration;
	public static ForgeroConfiguration.Deserializer deserializer;

	public static ForgeroConfiguration load() {
		if (deserializer == null) deserializer = new ForgeroConfigurationData.Deserializer();

		if (!Files.exists(configurationFilePath)) {
			return createConfigurationFile();
		}

		try (InputStream stream = Files.newInputStream(configurationFilePath)) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(ForgeroConfiguration.class, deserializer);

			Gson configurationGson = gsonBuilder.create();
			configuration = configurationGson.fromJson(new JsonReader(new BufferedReader(new InputStreamReader(stream))), ForgeroConfiguration.class);

			if (configuration.resourceLogging) {
				Forgero.LOGGER.info("(Re)loading Forgero configuration, located at: {}", configurationFilePath);
			}

			// TODO: Set configuration's available dependencies

			return configuration;
		} catch (IOException e) {
			Forgero.LOGGER.info("Unable to read Forgero configuration file, located at {}. See stack trace below", configurationFilePath);
			e.printStackTrace();
			return createConfigurationFile();
		}
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

			if (configuration.resourceLogging) {
				Forgero.LOGGER.info("(Re)created Forgero configuration file located at {}", configurationFilePath);
			}

			return forgeroConfiguration;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
