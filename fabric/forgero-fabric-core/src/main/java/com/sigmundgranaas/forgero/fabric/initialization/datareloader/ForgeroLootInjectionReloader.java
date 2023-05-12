package com.sigmundgranaas.forgero.fabric.initialization.datareloader;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.LootEntryData;
import com.sigmundgranaas.forgero.fabric.loot.TreasureInjector;
import com.sigmundgranaas.forgero.minecraft.common.loot.SingleLootEntry;

import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;

public class ForgeroLootInjectionReloader implements SimpleSynchronousResourceReloadListener {
	@Override
	public void reload(ResourceManager manager) {
		Gson gson = new Gson();
		TreasureInjector injector = TreasureInjector.getInstance();
		for (Resource res : manager.findResources("forgero_loot", path -> path.getPath().endsWith(".json")).values()) {
			try (InputStream stream = res.getInputStream()) {
				LootEntryData data = gson.fromJson(new JsonReader(new InputStreamReader(stream)), LootEntryData.class);
				injector.registerEntry(data.id(), SingleLootEntry.of(data));
			} catch (Exception e) {
				Forgero.LOGGER.error(e);
			}
		}
		TreasureInjector.getInstance().registerDynamicLoot();
	}

	@Override
	public Identifier getFabricId() {
		return ResourceReloadListenerKeys.LOOT_TABLES;
	}

}
