package com.sigmundgranaas.forgero.fabric.initialization;

import static com.sigmundgranaas.forgero.minecraft.common.entity.Entities.SOUL_ENTITY;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.property.active.ActivePropertyRegistry;
import com.sigmundgranaas.forgero.core.property.active.VeinBreaking;
import com.sigmundgranaas.forgero.core.registry.SoulLevelPropertyRegistry;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.SoulLevelPropertyData;
import com.sigmundgranaas.forgero.core.soul.SoulLevelPropertyDataProcessor;
import com.sigmundgranaas.forgero.fabric.api.entrypoint.ForgeroPreInitializationEntryPoint;
import com.sigmundgranaas.forgero.fabric.registry.DefaultLevelProperties;
import com.sigmundgranaas.forgero.minecraft.common.entity.Entities;
import com.sigmundgranaas.forgero.minecraft.common.entity.SoulEntity;
import com.sigmundgranaas.forgero.minecraft.common.property.handler.PatternBreaking;
import com.sigmundgranaas.forgero.minecraft.common.property.handler.TaggedPatternBreaking;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

public class ForgeroPreInit implements ForgeroPreInitializationEntryPoint {
	@Override
	public void onPreInitialization() {
		ActivePropertyRegistry.register(new ActivePropertyRegistry.PropertyEntry(PatternBreaking.predicate, PatternBreaking.factory));
		ActivePropertyRegistry.register(new ActivePropertyRegistry.PropertyEntry(TaggedPatternBreaking.predicate, TaggedPatternBreaking.factory));
		ActivePropertyRegistry.register(new ActivePropertyRegistry.PropertyEntry(VeinBreaking.predicate, VeinBreaking.factory));

		FabricDefaultAttributeRegistry.register(SOUL_ENTITY, SoulEntity.createSoulEntities());
		soulLevelPropertyReloader();
		DefaultLevelProperties.defaults().forEach(SoulLevelPropertyRegistry::register);
		Entities.register();
	}

	private void soulLevelPropertyReloader() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public void reload(ResourceManager manager) {
				SoulLevelPropertyRegistry.refresh();
				Gson gson = new Gson();
				for (Identifier res : manager.findResources("leveled_soul_properties", path -> path.endsWith(".json"))) {
					try (InputStream stream = manager.getResource(res).getInputStream()) {
						SoulLevelPropertyData data = gson.fromJson(new JsonReader(new InputStreamReader(stream)), SoulLevelPropertyData.class);
						SoulLevelPropertyRegistry.register(data.getId(), new SoulLevelPropertyDataProcessor(data));
					} catch (Exception e) {
						Forgero.LOGGER.error(e);
					}
				}
			}

			@Override
			public Identifier getFabricId() {
				return new Identifier(Forgero.NAMESPACE, "soul_level_property");
			}
		});
	}
}
