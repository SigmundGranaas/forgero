package com.sigmundgranaas.forgero.fabric;

import java.util.function.Supplier;

import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroInitializedEntryPoint;
import com.sigmundgranaas.forgero.fabric.tags.BountifulFares;
import com.sigmundgranaas.forgero.fabric.tags.Create;
import com.sigmundgranaas.forgero.fabric.tags.Ecologics;
import com.sigmundgranaas.forgero.fabric.tags.MythicMetalsCommons;
import com.sigmundgranaas.forgero.fabric.patchouli.BookDropOnAdvancement;
import com.sigmundgranaas.forgero.fabric.tags.NaturesSpirit;
import com.sigmundgranaas.forgero.fabric.tags.BiomesWeveGone;
import com.sigmundgranaas.forgero.fabric.tags.ModernIndustrialization;
import com.sigmundgranaas.forgero.fabric.tags.TechReborn;
import com.sigmundgranaas.forgero.fabric.tags.BloomingNature;
import com.sigmundgranaas.forgero.fabric.tags.BeachParty;
import com.sigmundgranaas.forgero.fabric.tags.Meadow;
import com.sigmundgranaas.forgero.fabric.toolstats.ToolStatTagGenerator;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.fabricmc.loader.api.FabricLoader;

public class ForgeroCompatInitializer implements ForgeroInitializedEntryPoint {
	public static final Supplier<Boolean> toolstats;
	public static final Supplier<Boolean> bettercombat;
	public static final Supplier<Boolean> mythicmetals;
	public static final Supplier<Boolean> yacl;
	public static final Supplier<Boolean> emi;
	public static final Supplier<Boolean> modonomicon;
	public static final Supplier<Boolean> beachparty;
	public static final Supplier<Boolean> modernindustrialization;
	public static final Supplier<Boolean> natures_spirit;
	public static final Supplier<Boolean> create;
	public static final Supplier<Boolean> ecologics;
	public static final Supplier<Boolean> biomeswevegone;

	public static final Supplier<Boolean> techreborn;
	public static final Supplier<Boolean> bloomingnature;
	public static final Supplier<Boolean> meadow;
	public static final Supplier<Boolean> bountifulfares;

	static {
		bountifulfares = () -> isModLoaded("bountifulfares");
		beachparty = () -> isModLoaded("beachparty");
		bloomingnature = () -> isModLoaded("bloomingnature");
		techreborn = () -> isModLoaded("techreborn");
		modernindustrialization = () -> isModLoaded("modernindustrialization");
		ecologics = () -> isModLoaded("ecologics");
		create = () -> isModLoaded("create");
		biomeswevegone = () -> isModLoaded("biomeswevegone");
		meadow = () -> isModLoaded("meadow");
		modonomicon = () -> isModLoaded("modonomicon");
		toolstats = () -> isModLoaded("toolstats");
		emi = () -> isModLoaded("emi");
		natures_spirit = () -> isModLoaded("natures_spirit");
		mythicmetals = () -> isModLoaded("mythicmetals");
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

		if (mythicmetals.get()) {
			MythicMetalsCommons.generateTags();
		}

		if (modernindustrialization.get()) {
			ModernIndustrialization.generateTags();
		}

		if (biomeswevegone.get()) {
			BiomesWeveGone.generateTags();
		}

		if (natures_spirit.get()) {
			NaturesSpirit.generateTags();
		}

		if (ecologics.get()) {
			Ecologics.generateTags();
		}

		if (create.get()) {
			Create.generateTags();
		}

		if (techreborn.get()) {
			TechReborn.generateTags();
		}

		if (bloomingnature.get()) {
			BloomingNature.generateTags();
		}

		if (beachparty.get()) {
			BeachParty.generateTags();
		}

		if (meadow.get()) {
			Meadow.generateTags();
		}

		if (bountifulfares.get()) {
			BountifulFares.generateTags();
		}
	}
}
