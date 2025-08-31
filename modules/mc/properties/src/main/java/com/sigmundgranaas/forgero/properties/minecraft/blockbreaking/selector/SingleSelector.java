package com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.selector;

import com.mojang.serialization.Codec;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record SingleSelector() implements BlockSelector {
	public static final String TYPE = "forgero:single";
	public static final SingleSelector INSTANCE = new SingleSelector();
	public static final Codec<SingleSelector> CODEC = Codec.unit(INSTANCE);

	@NotNull
	@Override
	public Set<BlockPos> select(BlockPos rootPos, Entity source) {
		return Set.of(rootPos);
	}

	@Override
	public String type() {
		return TYPE;
	}
}
