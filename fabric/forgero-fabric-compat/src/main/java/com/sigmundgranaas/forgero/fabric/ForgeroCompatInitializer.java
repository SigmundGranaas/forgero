package com.sigmundgranaas.forgero.fabric;

import java.util.function.Supplier;

import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroInitializedEntryPoint;
import com.sigmundgranaas.forgero.fabric.tags.CommonTags;
import com.sigmundgranaas.forgero.fabric.patchouli.BookDropOnAdvancement;
import com.sigmundgranaas.forgero.fabric.toolstats.ToolStatTagGenerator;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.fabricmc.loader.api.FabricLoader;

public class ForgeroCompatInitializer implements ForgeroInitializedEntryPoint {
	public static final Supplier<Boolean> toolstats;
	public static final Supplier<Boolean> bettercombat;
	public static final Supplier<Boolean> yacl;
	public static final Supplier<Boolean> emi;
	public static final Supplier<Boolean> modonomicon;

	static {
		modonomicon = () -> isModLoaded("modonomicon");
		toolstats = () -> isModLoaded("toolstats");
		emi = () -> isModLoaded("emi");
		bettercombat = () -> isModLoaded("bettercombat");
		yacl = () -> isModLoaded("yet-another-config-lib") || isModLoaded("yet_another_config_lib_v3");
	}

	public static boolean isModLoaded(String id) {
		return FabricLoader.getInstance().isModLoaded(id);
	}

	@Override
	public void onInitialized(StateService service) {
		if (toolstats.get()) {
			ToolStatTagGenerator.generateTags();
		}

		if (modonomicon.get()) {
			BookDropOnAdvancement.registerBookDrop();
		}

		// Goes through all mod compats that has common materials and checks if the mod is loaded and adds all of their tags to a separate resource pack
		// Each mod gets its own runtime resource pack to avoid them overriding each other.
		CommonTags.registerAndFilterCommonMaterialTags();
	}
}
