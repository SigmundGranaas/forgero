package com.sigmundgranaas.forgero.fabric;

import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroInitializedEntryPoint;
import com.sigmundgranaas.forgero.fabric.patchouli.BookDropOnAdvancement;
import com.sigmundgranaas.forgero.fabric.patchouli.GuideBookGenerator;
import com.sigmundgranaas.forgero.fabric.toolstats.ToolStatTagGenerator;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.fabricmc.loader.api.FabricLoader;

public class ForgeroCompatInitializer implements ForgeroInitializedEntryPoint {
	public static final boolean toolstats;
	public static final boolean patchouli;
	public static final boolean modmenu;
	public static final boolean bettercombat;

	static {
		toolstats = isModLoaded("toolstats");
		patchouli = isModLoaded("patchouli");
		modmenu = isModLoaded("modmenu");
		bettercombat = isModLoaded("bettercombat");
	}

	public static boolean isModLoaded(String id) {
		return FabricLoader.getInstance().isModLoaded(id);
	}

	@Override
	public void onInitialized(StateService service) {
		if (toolstats) {
			ToolStatTagGenerator.generateTags();
		}

		if (patchouli) {
			BookDropOnAdvancement.registerBookDrop();
			GuideBookGenerator.registerGuideBookRecipes();
		}
	}
}
