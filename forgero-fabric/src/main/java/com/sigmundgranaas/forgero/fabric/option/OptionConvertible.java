package com.sigmundgranaas.forgero.fabric.option;

import net.minecraft.client.option.SimpleOption;

public interface OptionConvertible {
	SimpleOption<?> asOption();
}
