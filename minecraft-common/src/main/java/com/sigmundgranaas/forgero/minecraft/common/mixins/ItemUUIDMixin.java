package com.sigmundgranaas.forgero.minecraft.common.mixins;

import net.minecraft.item.Item;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(Item.class)
public interface ItemUUIDMixin {
    @Accessor
    static UUID getATTACK_DAMAGE_MODIFIER_ID() {
        throw new NotImplementedException("IKeyBinding mixin failed to apply");
    }
}
