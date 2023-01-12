package com.sigmundgranaas.forgero.fabric;

import com.sigmundgranaas.forgero.fabric.patchouli.BookDropOnAdvancement;
import com.sigmundgranaas.forgero.fabric.patchouli.GuideBookGenerator;
import com.sigmundgranaas.forgero.fabric.toolstats.ToolStatTagGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ForgeroCompatInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("toolstats")) {
            ToolStatTagGenerator.generateTags();
        }

        if (FabricLoader.getInstance().isModLoaded("patchouli")) {
            BookDropOnAdvancement.registerBookDrop();
            GuideBookGenerator.registerGuideBookRecipes();
        }
    }
}
