package com.sigmundgranaas.forgero.minecraft.common.match.predicate;

import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.BLOCK_TARGET;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.WORLD;
import static com.sigmundgranaas.forgero.minecraft.common.match.predicate.EntityPredicateMatcher.EntityPredicateWriter.formatTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.sigmundgranaas.forgero.core.model.match.builders.ElementParser;
import com.sigmundgranaas.forgero.core.model.match.builders.PredicateBuilder;
import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import com.sigmundgranaas.forgero.core.property.v2.feature.JsonBuilder;
import com.sigmundgranaas.forgero.core.util.TypeToken;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.core.util.match.Matchable;
import com.sigmundgranaas.forgero.minecraft.common.handler.blockbreak.filter.BlockFilter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.PredicateWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.PredicateWriterBuilder;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.WriterHelper;

import net.minecraft.entity.Entity;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record BlockPredicateMatcher(BlockPredicate predicate) implements Matchable, BlockFilter {
	public static String ID = "minecraft:block";

	public final static ClassKey<BlockPredicateMatcher> KEY = new ClassKey<>(ID, BlockPredicateMatcher.class);

	public final static JsonBuilder<BlockPredicateMatcher> BUILDER = new BlockFilterBuilder();
	
	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		Optional<BlockPos> block = context.get(BLOCK_TARGET);
		Optional<World> world = context.get(WORLD);

		if (block.isPresent() && world.isPresent()) {
			return predicate.test(world.get(), block.get());
		}
		return false;
	}

	@Override
	public boolean filter(Entity entity, BlockPos currentPos, BlockPos root) {
		return predicate().test(entity.getWorld(), currentPos);
	}

	public static class BlockPredicateBuilder implements PredicateBuilder {

		@Override
		public Optional<Matchable> create(JsonElement element) {
			return ElementParser.fromIdentifiedElement(element, ID)
					.map(BlockPredicate::fromJson)
					.map(BlockPredicateMatcher::new);
		}
	}

	public static class BlockFilterBuilder implements JsonBuilder<BlockPredicateMatcher> {

		@Override
		public Optional<BlockPredicateMatcher> build(JsonElement element) {
			return ElementParser.fromIdentifiedElement(element, ID)
					.map(BlockPredicate::fromJson)
					.map(BlockPredicateMatcher::new);
		}

		@Override
		public TypeToken<BlockPredicateMatcher> getTargetClass() {
			return KEY.clazz();
		}
	}

	public static class BlockPredicateWriter implements PredicateWriter {
		private final BlockPredicate predicate;
		private final WriterHelper helper;

		public BlockPredicateWriter(BlockPredicate predicate, WriterHelper helper) {
			this.predicate = predicate;
			this.helper = helper;
		}

		public static PredicateWriterBuilder builder() {
			return (Matchable matchable, TooltipConfiguration configuration) -> {
				if (matchable instanceof BlockPredicateMatcher blockPredicateMatcher) {
					return Optional.of(new BlockPredicateMatcher.BlockPredicateWriter(blockPredicateMatcher.predicate, new WriterHelper(configuration.toBuilder().baseIndent(configuration.baseIndent() + 2).build())));
				}
				return Optional.empty();
			};
		}

		@Override
		public List<MutableText> write(Matchable matchable) {
			List<MutableText> tooltips = new ArrayList<>();

			// Handle Block Tag
			if (predicate.getTag() != null) {
				tooltips.add(helper.writeBase().append(Text.translatable("tooltip.forgero.against")
						.formatted(Formatting.GRAY)
						.append(Text.translatable(formatTag("tag", predicate.getTag().id())))));
			}

			// Handle Blocks
			if (predicate.getBlocks() != null && !predicate.getBlocks().isEmpty()) {
				MutableText blocksTooltip = Text.translatable("tooltip.forgero.blocks").formatted(Formatting.GRAY);
				predicate.getBlocks().forEach(block -> blocksTooltip.append(block.getName().getString()).append(", "));
				// Removing the trailing comma and space
				String blocksString = blocksTooltip.getString().substring(0, blocksTooltip.getString().length() - 2);
				tooltips.add(Text.literal(blocksString));
			}

			// Handle NBT
			if (predicate.getNbt() != NbtPredicate.ANY) {
				tooltips.add(Text.translatable("tooltip.forgero.nbt_present").formatted(Formatting.GRAY));
			}

			return tooltips;
		}
	}
}
