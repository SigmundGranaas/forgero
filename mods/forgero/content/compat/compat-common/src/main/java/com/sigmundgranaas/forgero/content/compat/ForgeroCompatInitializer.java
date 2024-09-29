package com.sigmundgranaas.forgero.content.compat;

import java.util.function.Supplier;

import com.sigmundgranaas.forgero.abstractions.utils.ModLoaderUtils;
import com.sigmundgranaas.forgero.api.v0.entrypoint.ForgeroInitializedEntryPoint;
import com.sigmundgranaas.forgero.content.compat.patchouli.BookDropOnAdvancement;
import com.sigmundgranaas.forgero.content.compat.tags.CommonTags;
import com.sigmundgranaas.forgero.content.compat.toolstats.ToolStatTagGenerator;
import com.sigmundgranaas.forgero.service.StateService;
import org.jetbrains.annotations.NotNull;

public class ForgeroCompatInitializer implements ForgeroInitializedEntryPoint {
	public static final Supplier<Boolean> toolstats;
	public static final Supplier<Boolean> bettercombat;
	public static final Supplier<Boolean> yacl;
	public static final Supplier<Boolean> emi;
	public static final Supplier<Boolean> modonomicon;

	static {
		modonomicon = () -> ModLoaderUtils.isModPresent("modonomicon");
		toolstats = () -> ModLoaderUtils.isModPresent("toolstats");
		emi = () -> ModLoaderUtils.isModPresent("emi");
		bettercombat = () -> ModLoaderUtils.isModPresent("bettercombat");
		yacl = () -> ModLoaderUtils.isModPresent("yet-another-config-lib") || ModLoaderUtils.isModPresent("yet_another_config_lib_v3");
	}

	@Override
	public void onInitialized(@NotNull StateService service) {
		if (toolstats.get()) {
			ToolStatTagGenerator.generateTags();
		}

		if (modonomicon.get()) {
			BookDropOnAdvancement.registerBookDrop();
		}

		// Goes through all mod compatibilities that have common materials, it checks if the mod is loaded and adds all of their tags to a separate resource pack
		// Each mod gets its own runtime resource pack to avoid them overriding each other
		CommonTags.registerAndFilterCommonMaterialTags();
	}
}
