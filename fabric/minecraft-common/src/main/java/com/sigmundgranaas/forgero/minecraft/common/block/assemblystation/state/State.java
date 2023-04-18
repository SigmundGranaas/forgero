package com.sigmundgranaas.forgero.minecraft.common.block.assemblystation.state;

public interface State {
	void onItemAddedToDeconstructionSlot();

	void onDeconstruction();

	void onRemoveItemFromDeconstructionSlot();

	void onReset();
}
