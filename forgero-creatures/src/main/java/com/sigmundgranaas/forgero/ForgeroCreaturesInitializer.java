package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.creatures.kuruk.KurukEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import static com.sigmundgranaas.forgero.creatures.CreatureTypes.KURUK;
import static com.sigmundgranaas.forgero.creatures.kuruk.KurukEntity.KURUK_ID;
import static net.minecraft.util.registry.Registry.ENTITY_TYPE;
import static net.minecraft.util.registry.Registry.ITEM;

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
            KURUK = Registry.register(ENTITY_TYPE, KURUK_ID, EntityType.Builder.create(KurukEntity::new, SpawnGroup.MISC).setDimensions(0.7F, 2.4F).maxTrackingRange(8).trackingTickInterval(2).build(KURUK_ID.toString()));
            FabricDefaultAttributeRegistry.register(KURUK, KurukEntity.createKurukEntityAttributes());
        }
    }
}
