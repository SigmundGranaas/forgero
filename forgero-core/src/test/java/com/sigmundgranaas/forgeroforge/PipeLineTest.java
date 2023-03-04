package com.sigmundgranaas.forgeroforge;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.core.resource.data.v2.packages.FilePackageLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.sigmundgranaas.forgero.core.ForgeroStateRegistry.*;
import static com.sigmundgranaas.forgero.core.resource.data.Constant.MINECRAFT_PACKAGE;
import static com.sigmundgranaas.forgero.core.resource.data.Constant.VANILLA_PACKAGE;

public class PipeLineTest {
	public static PipelineBuilder defaultResourcePipeLineTest() {
		return PipelineBuilder
				.builder()
				.register(() -> List.of(new FilePackageLoader(VANILLA_PACKAGE).get(), new FilePackageLoader(MINECRAFT_PACKAGE).get()))
				.state(stateListener())
				.state(compositeListener())
				.inflated(containerListener());
	}

	@Test
	void loadResources() {
		defaultResourcePipeLineTest().build().execute();
		Assertions.assertTrue(ForgeroStateRegistry.stateFinder().find("forgero:oak-pickaxe").isPresent());
	}
}
