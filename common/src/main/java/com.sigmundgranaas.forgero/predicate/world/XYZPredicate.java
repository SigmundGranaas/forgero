package com.sigmundgranaas.forgero.predicate.world;

import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.math.BlockPos;

public record XYZPredicate(double x, double y, double z) implements Predicate<BlockPos> {
	public static final Codec<XYZPredicate> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(
							Codec.DOUBLE.fieldOf("x").forGetter(XYZPredicate::x),
							Codec.DOUBLE.fieldOf("y").forGetter(XYZPredicate::y),
							Codec.DOUBLE.fieldOf("z").forGetter(XYZPredicate::z))
					.apply(instance, XYZPredicate::new));

	@Override
	public boolean test(BlockPos pos) {
		return pos.getX() == x && pos.getY() == y && pos.getZ() == z;
	}
}
