package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.command.CommandRegistry;
import com.sigmundgranaas.forgero.item.ItemGroups;
import com.sigmundgranaas.forgero.item.items.testutil.Tools;
import com.sigmundgranaas.forgero.item.items.tool.DynamicSwordItem;
import com.sigmundgranaas.forgero.loot.TreasureInjector;
import com.sigmundgranaas.forgero.registry.CustomItemRegistry;
import com.sigmundgranaas.forgero.registry.ForgeroItemRegistry;
import com.sigmundgranaas.forgero.registry.RecipeRegistry;
import com.sigmundgranaas.forgero.registry.impl.MineCraftRegistryHandler;
import com.sigmundgranaas.forgero.resources.DynamicResourceGenerator;
import com.sigmundgranaas.forgero.resources.ModContainerService;
import com.sigmundgranaas.forgero.resources.loader.FabricResourceLoader;
import com.sigmundgranaas.forgero.resources.loader.ReloadableResourceLoader;
import com.sigmundgranaas.forgero.resources.loader.ResourceManagerStreamProvider;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.ToolMaterials;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ForgeroInitializer implements ModInitializer {
    public static final String MOD_NAMESPACE = "forgero";
    public static final Logger LOGGER = LogManager.getLogger(ForgeroInitializer.MOD_NAMESPACE);

    @Override
    public void onInitialize() {
        var loader = new FabricResourceLoader(new ModContainerService().getAllModsAsSet());
        var registry = ForgeroItemRegistry.INSTANCE.loadResourcesIfEmpty(loader);
        registry.register(new MineCraftRegistryHandler());
        resourceReloader();
        new CustomItemRegistry().register();
        registerRecipes();
        new CommandRegistry().registerCommand();
        new TreasureInjector().registerLoot();
        new DynamicResourceGenerator().generateResources();
        DynamicSwordItem oakSword = new DynamicSwordItem(ToolMaterials.WOOD, 1, 1, new FabricItemSettings().group(ItemGroups.FORGERO_TOOL_PARTS), Tools.IRON_PICKAXE);
        Registry.register(Registry.ITEM, new Identifier("minecraft", oakSword.name()), oakSword);
    }

    private void registerRecipes() {
        RecipeRegistry.INSTANCE.registerRecipeSerializers();
    }


    private void resourceReloader() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public void reload(ResourceManager manager) {
                ForgeroItemRegistry.INSTANCE.updateResources(new ReloadableResourceLoader(new ModContainerService().getAllModsAsSet(), new ResourceManagerStreamProvider(manager)));
            }

            @Override
            public Identifier getFabricId() {
                return new Identifier(ForgeroInitializer.MOD_NAMESPACE, "data");
            }
        });
    }
}
