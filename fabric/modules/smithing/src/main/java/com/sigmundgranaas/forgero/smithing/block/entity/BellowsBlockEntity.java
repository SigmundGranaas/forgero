package com.sigmundgranaas.forgero.smithing.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class BellowsBlockEntity extends BlockEntity {
	private float animationProgress = 0f;   // 0 = expanded, 1 = fully contracted
	private boolean animating = false;      // is the bellows moving right now
	private boolean contracting = true;     // true = contracting, false = expanding

	public BellowsBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BELLOWS, pos, state);
	}

	/** Called when the player right-clicks */
	public void startPumping() {
		if (!animating) {
			animating = true;
			contracting = true;
			markDirty();
		}
	}

	public float getAnimationProgress() {
		return animationProgress;
	}


	public void tick() {
		if (world == null) return; // tick on both client and server

		if (animating) {
			if (contracting) {
				animationProgress += 0.05f; // contraction speed
				if (animationProgress >= 1f) {
					animationProgress = 1f;
					contracting = false; // switch to expanding
				}
			} else {
				animationProgress -= 0.05f; // expansion speed
				if (animationProgress <= 0f) {
					animationProgress = 0f;
					animating = false; // ✅ stop after one full cycle
				}
			}
			markDirty();
		}
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.putFloat("AnimProgress", animationProgress);
		nbt.putBoolean("Animating", animating);
		nbt.putBoolean("Contracting", contracting);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		animationProgress = nbt.getFloat("AnimProgress");
		animating = nbt.getBoolean("Animating");
		contracting = nbt.getBoolean("Contracting");
	}
}
