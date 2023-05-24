package com.sigmundgranaas.forgero.fabric.initialization.datareloader;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.condition.Conditions;
import com.sigmundgranaas.forgero.core.condition.LootCondition;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ConditionData;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

public class LootConditionReloadListener implements SimpleSynchronousResourceReloadListener {
	@Override
	public void reload(ResourceManager manager) {
		Conditions.INSTANCE.refresh();
		Gson gson = new Gson();
		for (Resource res : manager.findResources("conditions", path -> path.getPath().endsWith(".json")).values()) {
			try (InputStream stream = res.getInputStream()) {
				ConditionData data = gson.fromJson(new JsonReader(new InputStreamReader(stream)), ConditionData.class);
				LootCondition.of(data).ifPresent(Conditions.INSTANCE::register);
			} catch (Exception e) {
				Forgero.LOGGER.error(e);
			}
		}
	}

	@Override
	public Identifier getFabricId() {
		return new Identifier(Forgero.NAMESPACE, "loot_condition");
	}

}
