package com.sigmundgranaas.forgeroforge.test.util;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.core.resource.data.v2.PackageSupplier;
import com.sigmundgranaas.forgero.core.resource.data.v2.packages.FilePackageLoader;

import java.nio.file.Path;
import java.util.List;

import static com.sigmundgranaas.forgero.core.ForgeroStateRegistry.*;
import static com.sigmundgranaas.forgero.core.resource.data.Constant.*;
import static com.sigmundgranaas.forgero.vanilla.ForgeroVanilla.VANILLA_SUPPLIER;

public class ForgeroPipeLineSetup {

	public static PackageSupplier EXTENDED_SUPPLIER = () -> List.of(new FilePackageLoader(EXTENDED_PACKAGE).get(), new FilePackageLoader(TRINKETS_PACKAGE).get(), new FilePackageLoader(SWORD_ADDITIONS_PACKAGE).get(), new FilePackageLoader(MINECRAFT_EXTENDED_PACKAGE).get());

	public static void setup() {
		if (ForgeroStateRegistry.COMPOSITES == null) {
			ForgeroConfigurationLoader.load(Path.of("config"));
			PipelineBuilder
					.builder()
					.register(VANILLA_SUPPLIER)
					.register(EXTENDED_SUPPLIER)
					.state(stateListener())
					.state(compositeListener())
					.inflated(containerListener())
					.build()
					.execute();
		}
	}
}
