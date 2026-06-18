package com.sigmundgranaas.forgero.effects.mark;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

/**
 * Server-side store for transient "marks" on entities, backed by per-entity persistent NBT
 * (see {@code LivingEntityMarkDataMixin}). A mark is a named flag with an absolute expiry in world
 * game-time; expiry is evaluated lazily on read, so there is no per-tick scan and no orphaned
 * state to clean up — the data lives and dies with the entity and survives save/reload and
 * dimension changes.
 */
public final class MarkStore {
	private MarkStore() {
	}

	/**
	 * Marks the entity until {@code world.getTime() + durationTicks}. Re-marking refreshes expiry.
	 *
	 * @throws IllegalArgumentException if durationTicks is not positive
	 */
	public static void mark(LivingEntity entity, Identifier markId, int durationTicks) {
		if (durationTicks <= 0) {
			throw new IllegalArgumentException("mark duration must be > 0, got: " + durationTicks);
		}
		long expiry = entity.getWorld().getTime() + durationTicks;
		data(entity).putLong(markId.toString(), expiry);
	}

	/**
	 * @return true if the entity currently carries an unexpired mark with this id. Expired marks are
	 * pruned on access.
	 */
	public static boolean hasMark(LivingEntity entity, Identifier markId) {
		NbtCompound data = data(entity);
		String key = markId.toString();
		if (!data.contains(key)) {
			return false;
		}
		long expiry = data.getLong(key);
		if (entity.getWorld().getTime() >= expiry) {
			data.remove(key);
			return false;
		}
		return true;
	}

	public static void clearMark(LivingEntity entity, Identifier markId) {
		data(entity).remove(markId.toString());
	}

	private static NbtCompound data(LivingEntity entity) {
		return ((MarkDataHolder) entity).forgero$getMarkData();
	}
}
