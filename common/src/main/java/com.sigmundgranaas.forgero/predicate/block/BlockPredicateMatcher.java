package com.sigmundgranaas.forgero.predicate.block;

import static com.sigmundgranaas.forgero.api.v0.predicate.Registries.BLOCK_CODEC_REGISTRY;

import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.handler.blockbreak.filter.BlockFilter;
import com.sigmundgranaas.forgero.predicate.codecs.GroupEntry;
import com.sigmundgranaas.forgero.predicate.codecs.KeyPair;
import com.sigmundgranaas.forgero.predicate.codecs.SpecificationBackedPredicateCodec;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;


public class BlockPredicateMatcher implements Predicate<WorldBlockPair>, Matchable, BlockFilter {
	private final GroupEntry<KeyPair<Predicate<WorldBlockPair>>> entry;
	public static final String TYPE = "minecraft:block";
	public static final SpecificationBackedPredicateCodec<WorldBlockPair> BLOCK_CODEC = new SpecificationBackedPredicateCodec<>(TYPE, BLOCK_CODEC_REGISTRY);
	public static final Codec<BlockPredicateMatcher> CODEC = new MapCodec.MapCodecCodec<>(BLOCK_CODEC.xmap(BlockPredicateMatcher::new, blockPredicate -> blockPredicate.entry));

	public BlockPredicateMatcher(GroupEntry<KeyPair<Predicate<WorldBlockPair>>> entry) {
		this.entry = entry;
	}

	@Override
	public boolean test(WorldBlockPair pair) {
		if (entry.entries().isEmpty()) {
			return false;
		}
		return entry.entries().stream()
				.map(KeyPair::value)
				.allMatch(spec -> spec.test(pair));
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		return WorldBlockPair.of(context)
				.map(this::test)
				.orElse(false);
	}

	@Override
	public boolean filter(Entity entity, BlockPos currentPos, BlockPos root) {
		return test(new WorldBlockPair(entity.getWorld(), currentPos));
	}
}

