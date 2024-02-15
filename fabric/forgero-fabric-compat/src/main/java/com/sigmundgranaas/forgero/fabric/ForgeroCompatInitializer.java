package com.sigmundgranaas.forgero.fabric;

import java.util.function.Supplier;

import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroInitializedEntryPoint;
import com.sigmundgranaas.forgero.fabric.mythicmetals.MythicMetalsCommons;
import com.sigmundgranaas.forgero.fabric.patchouli.BookDropOnAdvancement;
import com.sigmundgranaas.forgero.fabric.patchouli.GuideBookGenerator;
import com.sigmundgranaas.forgero.fabric.toolstats.ToolStatTagGenerator;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.fabricmc.loader.api.FabricLoader;

public class ForgeroCompatInitializer implements ForgeroInitializedEntryPoint {
	public static final Supplier<Boolean> toolstats;
	public static final Supplier<Boolean> patchouli;
	public static final Supplier<Boolean> bettercombat;
	public static final Supplier<Boolean> mythicmetals;
	public static final Supplier<Boolean> yacl;
	public static final Supplier<Boolean> emi;


	static {
		toolstats = () -> isModLoaded("toolstats");
		emi = () -> isModLoaded("emi");
		mythicmetals = () -> isModLoaded("mythicmetals");
		patchouli = () -> isModLoaded("patchouli");
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

		if(emi.get()){
			ForgeroConfigurationLoader.configuration.buildModelsAsync = false;
		}

		if (patchouli.get()) {
			BookDropOnAdvancement.registerBookDrop();
			GuideBookGenerator.registerGuideBookRecipes();
		}

		if (mythicmetals.get()) {
			MythicMetalsCommons.generateTags();
		}
	}
}
