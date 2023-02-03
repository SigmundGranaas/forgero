package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.creatures.kuruk.KurukEntity;
import com.sigmundgranaas.forgero.creatures.stonegolem.StoneGolemEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;

import static com.sigmundgranaas.forgero.creatures.CreatureTypes.KURUK;
import static com.sigmundgranaas.forgero.creatures.CreatureTypes.STONE_GOLEM;
import static com.sigmundgranaas.forgero.creatures.kuruk.KurukEntity.KURUK_ID;
import static com.sigmundgranaas.forgero.creatures.stonegolem.StoneGolemEntity.STONE_GOLEM_ID;
import static net.minecraft.util.registry.Registry.*;

public class ForgeroCreaturesInitializer implements ModInitializer {
    public static Item KURUK_HEART;
    public static Item KURUK_PELT;

    static {
        KURUK_HEART = Registry.register(ITEM, new Identifier("forgero:kuruk_heart"), new Item(new Item.Settings().rarity(Rarity.RARE).group(ItemGroup.MISC)));
        KURUK_PELT = Registry.register(ITEM, new Identifier("forgero:kuruk_pelt"), new Item(new Item.Settings().rarity(Rarity.RARE).group(ItemGroup.MISC)));
    }

    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("geckolib3")) {
            KURUK = Registry.register(ENTITY_TYPE, KURUK_ID, EntityType.Builder.create(KurukEntity::new, SpawnGroup.CREATURE).setDimensions(0.7F, 2.4F).maxTrackingRange(8).trackingTickInterval(2).build(KURUK_ID.toString()));
            FabricDefaultAttributeRegistry.register(KURUK, KurukEntity.createKurukEntityAttributes());
            BiomeModifications.addSpawn(BiomeSelectors.tag(TagKey.of(BIOME_KEY, new Identifier("forgero:is_any_forest"))), SpawnGroup.CREATURE, KURUK, 1, 1, 1);
            SpawnRestriction.register(KURUK, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, AnimalEntity::isValidNaturalSpawn);

            STONE_GOLEM = Registry.register(ENTITY_TYPE, STONE_GOLEM_ID, EntityType.Builder.create(StoneGolemEntity::new, SpawnGroup.CREATURE).setDimensions(1.4f, 2.7f).maxTrackingRange(10).build(STONE_GOLEM_ID.toString()));
            FabricDefaultAttributeRegistry.register(STONE_GOLEM, StoneGolemEntity.createStoneGolemEntityAttributes());
            BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, STONE_GOLEM, 3, 1, 1);
            SpawnRestriction.register(STONE_GOLEM, SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, StoneGolemEntity::isValidSpawn);
        }
    }
}
