package com.sigmundgranaas.forgero.fabric.option;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import net.minecraft.client.option.SimpleOption;

import java.util.List;

public class StringListConfigOption implements OptionConvertible {
	private final String key, translationKey;
	private final List<String> defaultValue;

	public StringListConfigOption(String key, List<String> defaultValue) {
		super();

		this.key = key;
		this.translationKey = String.format("option.%s", key);
		this.defaultValue = defaultValue;
	}

	public void set(List<String> value) {
		ForgeroConfigurationLoader.configuration.setByKey(key, value);
	}

	@SuppressWarnings("unchecked")
	public List<String> get() {
		return (List<String>) ForgeroConfigurationLoader.configuration.getByKey(key);
	}

	@Override
	public SimpleOption<List<String>> asOption() {
//		return new SimpleOption<List<String>>(
//				translationKey,
//				SimpleOption.emptyTooltip(),
//				(text, value) -> Text.translatable(this.translationKey),
//				new SimpleOption.PotentialValuesBasedCallbacks<List<String>>(get(), Codec.STRING.),
//				defaultValue,
//				value -> ForgeroSettings.SETTINGS.setByKey(this.key, value)
//		);
		return null;
	}
}
