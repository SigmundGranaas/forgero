package com.sigmundgranaas.forgero;

import com.sigmundgranaas.forgero.creatures.kuruk.KurukEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.registry.Registry;

import static com.sigmundgranaas.forgero.creatures.CreatureTypes.KURUK;
import static com.sigmundgranaas.forgero.creatures.kuruk.KurukEntity.KURUK_ID;
import static net.minecraft.util.registry.Registry.ENTITY_TYPE;

public class ForgeroCreaturesInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        if (FabricLoader.getInstance().isModLoaded("geckolib3")) {
            KURUK = Registry.register(ENTITY_TYPE, KURUK_ID, EntityType.Builder.create(KurukEntity::new, SpawnGroup.MISC).setDimensions(0.7F, 2.4F).maxTrackingRange(8).trackingTickInterval(2).build(KURUK_ID.toString()));
            FabricDefaultAttributeRegistry.register(KURUK, AnimalEntity.createLivingAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE));
        }
    }
}
