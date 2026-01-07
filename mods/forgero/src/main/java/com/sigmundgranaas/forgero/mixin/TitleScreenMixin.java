package com.sigmundgranaas.forgero.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
	@Unique
	private static boolean forgero$hasAutoLoaded = false;

	@Inject(method = "init", at = @At("TAIL"))
	private void autoLoadLastWorld(CallbackInfo ci) {
		if (forgero$hasAutoLoaded) {
			return;
		}
		forgero$hasAutoLoaded = true;

		MinecraftClient client = MinecraftClient.getInstance();

		try {
			LevelStorage levelStorage = client.getLevelStorage();
			List<LevelSummary> levels = levelStorage.loadSummaries(levelStorage.getLevelList()).join();

			if (levels.isEmpty()) {
				return;
			}

			LevelSummary mostRecent = levels.stream()
					.max(Comparator.comparingLong(LevelSummary::getLastPlayed))
					.orElse(null);

			if (mostRecent != null && !mostRecent.isLocked()) {
				client.createIntegratedServerLoader().start((TitleScreen) (Object) this, mostRecent.getName());
			}
		} catch (Exception e) {
			System.err.println("[Forgero] Failed to auto-load last world: " + e.getMessage());
		}
	}
}
