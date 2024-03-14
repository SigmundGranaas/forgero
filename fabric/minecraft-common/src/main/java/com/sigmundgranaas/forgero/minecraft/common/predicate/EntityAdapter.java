package com.sigmundgranaas.forgero.minecraft.common.predicate;

import java.util.function.Function;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class EntityAdapter {
	static Function<Entity, BlockPos> pos = Entity::getBlockPos;
	static Function<Entity, World> world = Entity::getWorld;
	static Function<Entity, Pair<BlockPos, WorldView>> posWorldPair = entity -> new Pair<>(entity.getBlockPos(), entity.getWorld());


	public static Codec<KeyPair<Predicate<Entity>>> entityPosCodec() {
		return AdapterCodec.of("pos", XYZPredicate.GENERAL_CODEC, PredicateAdapter.create(pos));
	}
}
