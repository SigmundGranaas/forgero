package com.sigmundgranaas.forgero.api.v0.predicate;

import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.predicate.codecs.KeyPair;
import com.sigmundgranaas.forgero.predicate.block.WorldBlockPair;
import com.sigmundgranaas.forgero.predicate.codecs.SpecificationRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class Registries {
	// Entity
	public static final SpecificationRegistry<Predicate<Entity>> ENTITY_FLAG_PREDICATE_REGISTRY = new SpecificationRegistry<>();
	public static final SpecificationRegistry<Codec<KeyPair<Predicate<Entity>>>> ENTITY_CODEC_REGISTRY = new SpecificationRegistry<>();

	// Blocks
	public static final SpecificationRegistry<Codec<KeyPair<Predicate<WorldBlockPair>>>> BLOCK_CODEC_REGISTRY = new SpecificationRegistry<>();

	// World
	public static final SpecificationRegistry<Codec<KeyPair<Predicate<World>>>> WORLD_CODEC_REGISTRY = new SpecificationRegistry<>();
}