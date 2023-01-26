package com.sigmundgranaas.forgero.minecraft.common.entity;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.soul.Soul;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import static com.sigmundgranaas.forgero.minecraft.common.entity.Entities.SOUL_ENTITY;

public class SoulEntity extends PathAwareEntity {
    public static final Identifier SOUL_ENTITIY_IDENTIFIER = new Identifier(Forgero.NAMESPACE, "soul_entity");
    private final Soul soul;

    public SoulEntity(World world, Soul soul, double x, double y, double z) {
        super(SOUL_ENTITY, world);
        this.setPosition(x, y, z);
        this.soul = soul;
    }

    public SoulEntity(World world, Soul soul) {
        super(SOUL_ENTITY, world);
        this.soul = soul;
    }

    public SoulEntity(EntityType<? extends SoulEntity> type, World world) {
        super(type, world);
        this.soul = new Soul();
    }

    public Soul getSoul() {
        return soul;
    }
}
