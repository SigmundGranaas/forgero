package com.sigmundgranaas.forgero.quilt;

import com.sigmundgranaas.forgero.quilt.patchouli.BookDropOnAdvancement;
import com.sigmundgranaas.forgero.quilt.patchouli.GuideBookGenerator;
import com.sigmundgranaas.forgero.quilt.toolstats.ToolStatTagGenerator;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ForgeroCompatInitializer implements ModInitializer {
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

	@Override
	public void onInitialize() {
		if (toolstats) {
			ToolStatTagGenerator.generateTags();
		}

		if (patchouli) {
			BookDropOnAdvancement.registerBookDrop();
			GuideBookGenerator.registerGuideBookRecipes();
		}
	}

	public static boolean isModLoaded(String id) {
		return FabricLoader.getInstance().isModLoaded(id);
	}
}
