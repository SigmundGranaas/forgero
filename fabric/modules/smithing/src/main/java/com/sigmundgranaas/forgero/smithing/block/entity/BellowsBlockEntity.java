package com.sigmundgranaas.forgero.smithing.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

public class BellowsBlockEntity extends BlockEntity {
	private float rotation = 0f;
	private float animationProgress = 0f; // for pumping animation later

	public BellowsBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.BELLOWS, pos, state);
	}

	public void setRotation(float rotation) {
		this.rotation = rotation % 360f;
		markDirty();
	}

	public float getRotation() {
		return rotation;
	}

	public void setAnimationProgress(float progress) {
		this.animationProgress = progress;
	}

	public float getAnimationProgress() {
		return animationProgress;
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.putFloat("Rotation", rotation);
		nbt.putFloat("Anim", animationProgress);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		rotation = nbt.getFloat("Rotation");
		animationProgress = nbt.getFloat("Anim");
	}
}
