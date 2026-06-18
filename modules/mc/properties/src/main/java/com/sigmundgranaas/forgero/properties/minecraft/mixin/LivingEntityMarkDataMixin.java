package com.sigmundgranaas.forgero.properties.minecraft.mixin;

import com.sigmundgranaas.forgero.effects.mark.MarkDataHolder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds a persistent NBT compound to every {@link LivingEntity} for Forgero marks, serialized
 * alongside the entity's own custom data so it survives save/reload and dimension changes.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMarkDataMixin implements MarkDataHolder {
	@Unique
	private static final String FORGERO_MARKS_KEY = "ForgeroMarks";

	@Unique
	private NbtCompound forgero$markData = new NbtCompound();

	@Override
	public NbtCompound forgero$getMarkData() {
		return forgero$markData;
	}

	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	private void forgero$writeMarks(NbtCompound nbt, CallbackInfo ci) {
		if (!forgero$markData.isEmpty()) {
			nbt.put(FORGERO_MARKS_KEY, forgero$markData.copy());
		}
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	private void forgero$readMarks(NbtCompound nbt, CallbackInfo ci) {
		if (nbt.contains(FORGERO_MARKS_KEY, NbtElement.COMPOUND_TYPE)) {
			forgero$markData = nbt.getCompound(FORGERO_MARKS_KEY).copy();
		}
	}
}
