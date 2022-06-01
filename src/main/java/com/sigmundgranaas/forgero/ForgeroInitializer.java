package com.sigmundgranaas.forgero;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sigmundgranaas.forgero.command.CommandRegistry;
import com.sigmundgranaas.forgero.core.ForgeroRegistry;
import com.sigmundgranaas.forgero.core.data.factory.SchematicFactory;
import com.sigmundgranaas.forgero.core.data.v1.pojo.SchematicPojo;
import com.sigmundgranaas.forgero.core.schematic.Schematic;
import com.sigmundgranaas.forgero.core.schematic.SchematicLoader;
import com.sigmundgranaas.forgero.loot.TreasureInjector;
import com.sigmundgranaas.forgero.registry.CustomItemRegistry;
import com.sigmundgranaas.forgero.registry.ForgeroItemRegistry;
import com.sigmundgranaas.forgero.registry.RecipeRegistry;
import com.sigmundgranaas.forgero.registry.impl.MineCraftRegistryHandler;
import com.sigmundgranaas.forgero.resources.DynamicResourceGenerator;
import com.sigmundgranaas.forgero.resources.ModContainerService;
import com.sigmundgranaas.forgero.resources.loader.FabricResourceLoader;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ForgeroInitializer implements ModInitializer {
    public static final String MOD_NAMESPACE = "forgero";
    public static final Logger LOGGER = LogManager.getLogger(ForgeroInitializer.MOD_NAMESPACE);

    @Override
    public void onInitialize() {
        var loader = new FabricResourceLoader(new ModContainerService().getForgeroResourceNamespaces());
        var registry = ForgeroItemRegistry.INSTANCE.loadResourcesIfEmpty(loader);
        registry.register(new MineCraftRegistryHandler());
        resourceReloader();
        new CustomItemRegistry().register();
        registerRecipes();
        new CommandRegistry().registerCommand();
        new TreasureInjector().registerLoot();
        new DynamicResourceGenerator().generateResources();

    }

    private void registerRecipes() {
        RecipeRegistry.INSTANCE.registerRecipeSerializers();
    }


    private void resourceReloader() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public void reload(ResourceManager manager) {
                var pojos = new ArrayList<SchematicPojo>();
                for (Identifier id : manager.findResources("schematics", path -> path.endsWith(".json"))) {
                    try (InputStream stream = manager.getResource(id).getInputStream()) {
                        if (stream != null) {
                            JsonReader materialsJson = new JsonReader(new InputStreamReader(stream));
                            SchematicPojo gson = new Gson().fromJson(materialsJson, SchematicPojo.class);
                            pojos.add(gson);
                        }
                    } catch (Exception e) {
                        ForgeroInitializer.LOGGER.error("Error occurred while loading resource json " + id.toString(), e);
                    }
                }
                var factory = new SchematicFactory(pojos, Set.of("forgero", "minecraft"));
                List<Schematic> schematics = pojos.stream().map(factory::buildResource).flatMap(Optional::stream).toList();

                if (schematics.isEmpty()) {
                    schematics = new SchematicLoader().loadSchematics();
                }
                ForgeroInitializer.LOGGER.info("Reloaded {} schematics", schematics.size());
                ForgeroRegistry.INSTANCE.SCHEMATIC.updateRegistry(schematics);
            }

            @Override
            public Identifier getFabricId() {
                return new Identifier(ForgeroInitializer.MOD_NAMESPACE, "schematics");
            }
        });
    }
}
