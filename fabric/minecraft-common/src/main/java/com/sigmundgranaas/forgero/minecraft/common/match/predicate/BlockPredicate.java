package com.sigmundgranaas.forgero.minecraft.common.match.predicate;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@NoArgsConstructor
@Getter
@AllArgsConstructor
@Builder
public class BlockPredicate {

	public static final BlockPredicate ANY = new BlockPredicate(null, null, StatePredicate.ANY, NbtPredicate.ANY);

	@Nullable
	private TagKey<Block> tag;

	@Nullable
	private Set<Block> blocks;

	private StatePredicate state;
	private NbtPredicate nbt;

	public static BlockPredicate fromJson(@Nullable JsonElement json) {
		if (json == null || json.isJsonNull()) {
			return BlockPredicate.ANY;
		}

		JsonObject jsonObject = JsonHelper.asObject(json, "block");
		NbtPredicate nbtPredicate = NbtPredicate.fromJson(jsonObject.get("nbt"));

		Set<Block> blocks = parseBlocks(jsonObject);
		TagKey<Block> tag = parseTag(jsonObject);

		StatePredicate state = StatePredicate.fromJson(jsonObject.get("state"));

		return BlockPredicate.builder()
				.tag(tag)
				.blocks(blocks)
				.state(state)
				.nbt(nbtPredicate)
				.build();
	}

	private static Set<Block> parseBlocks(JsonObject jsonObject) {
		JsonArray jsonArray = JsonHelper.getArray(jsonObject, "blocks", null);
		if (jsonArray == null) {
			return null;
		}

		ImmutableSet.Builder<Block> builder = ImmutableSet.builder();
		for (JsonElement jsonElement : jsonArray) {
			Identifier identifier = new Identifier(JsonHelper.asString(jsonElement, "block"));
			builder.add(Registries.BLOCK.getOrEmpty(identifier).orElseThrow(() ->
					new JsonSyntaxException("Unknown block id '" + identifier + "'")
			));
		}

		return builder.build();
	}

	private static TagKey<Block> parseTag(JsonObject jsonObject) {
		if (!jsonObject.has("tag")) {
			return null;
		}

		Identifier identifier = new Identifier(JsonHelper.getString(jsonObject, "tag"));
		return TagKey.of(Registries.BLOCK.getKey(), identifier);
	}

	public boolean test(World world, BlockPos pos) {
		if (this == ANY) {
			return true;
		}

		if (!world.canSetBlock(pos)) {
			return false;
		}

		BlockState blockState = world.getBlockState(pos);
		if (tag != null && !blockState.isIn(tag)) {
			return false;
		}

		if (blocks != null && !blocks.contains(blockState.getBlock())) {
			return false;
		}

		if (!state.test(blockState)) {
			return false;
		}

		if (nbt != NbtPredicate.ANY) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity == null) {
				return false;
			}
			return nbt.test(blockEntity.createNbtWithIdentifyingData());
		}

		return true;
	}
}
