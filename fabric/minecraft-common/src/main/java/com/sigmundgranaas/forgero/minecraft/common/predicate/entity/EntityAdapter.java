package com.sigmundgranaas.forgero.minecraft.common.predicate.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.minecraft.common.predicate.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.function.Function;

public class EntityAdapter {
	static Function<Entity, BlockPos> pos = Entity::getBlockPos;
	static Function<Entity, EntityType<?>> type = Entity::getType;
	static Function<Entity, World> world = Entity::getWorld;
	static Function<Entity, Pair<BlockPos, WorldView>> posWorldPair = entity -> new Pair<>(entity.getBlockPos(), entity.getWorld());

	public static String ENTITY_POS_KEY = "pos";
	public static String ENTITY_TYPE_KEY = "entity_type";


	public static Codec<KeyPair<Predicate<Entity>>> entityPosCodec() {
		return AdapterCodec.of(ENTITY_POS_KEY, XYZPredicate.GENERAL_CODEC, PredicateAdapter.create(pos));
	}

	public static Codec<KeyPair<Predicate<Entity>>> entityTypePredicate() {
		return AdapterCodec.of(ENTITY_TYPE_KEY, EntityTypePredicate.GENERAL_CODEC, PredicateAdapter.create(type));
	}
}
