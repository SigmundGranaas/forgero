package com.sigmundgranaas.forgero.effects.mark;

import net.minecraft.nbt.NbtCompound;

/**
 * Duck-typed accessor implemented on {@code LivingEntity} via mixin, exposing the persistent
 * compound that backs Forgero "marks". All access goes through {@link MarkStore}; callers should
 * not read or mutate the compound directly.
 */
public interface MarkDataHolder {
	NbtCompound forgero$getMarkData();
}
