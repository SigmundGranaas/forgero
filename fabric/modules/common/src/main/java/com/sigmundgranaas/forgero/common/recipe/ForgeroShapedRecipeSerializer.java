package com.sigmundgranaas.forgero.common.recipe;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.codec.KeyMapDispatchCodec;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ForgeroShapedRecipeSerializer implements RecipeSerializer<ForgeroShapedRecipe> {
	public static ForgeroShapedRecipeSerializer INSTANCE;
	public static final Identifier ID = new Identifier("forgero", "shaped_recipe");

	private final Codec<RecipeOutput> outputCodec;
	private final Codec<Map<Character, RecipeIngredient>> keyCodec;
	private final KeyMapDispatchCodec propertyDispatchCodec;

	public ForgeroShapedRecipeSerializer(Map<PropertyKey<?>, Codec<? extends List<?>>> propertyCodecs) {
		this.outputCodec = RecipeOutput.codec(propertyCodecs);
		this.keyCodec = Codec.unboundedMap(Codec.STRING.xmap(s -> s.charAt(0), String::valueOf), RecipeIngredient.CODEC);
		this.propertyDispatchCodec = new KeyMapDispatchCodec(propertyCodecs);
	}

	@Override
	public ForgeroShapedRecipe read(Identifier id, JsonObject json) {
		String group = JsonHelper.getString(json, "group", "");
		CraftingRecipeCategory category = CraftingRecipeCategory.CODEC.byId(JsonHelper.getString(json, "category", null), CraftingRecipeCategory.MISC);
		String[] pattern = ShapedRecipe.removePadding(ShapedRecipe.getPattern(JsonHelper.getArray(json, "pattern")));
		int width = pattern[0].length();
		int height = pattern.length;

		Map<Character, RecipeIngredient> forgeroKey = keyCodec.parse(JsonOps.INSTANCE, JsonHelper.getObject(json, "key"))
				.getOrThrow(false, msg -> LoggerFactory.getLogger(ForgeroShapedRecipeSerializer.class).error("Error parsing forgero key: {}", msg));

		Map<String, Ingredient> vanillaKey = forgeroKey.entrySet().stream()
				.collect(Collectors.toMap(e -> String.valueOf(e.getKey()), e -> e.getValue().toVanillaIngredient()));
		vanillaKey.put(" ", Ingredient.EMPTY);

		DefaultedList<Ingredient> ingredients = ShapedRecipe.createPatternMatrix(pattern, vanillaKey, width, height);

		RecipeOutput forgeroResult = outputCodec.parse(JsonOps.INSTANCE, JsonHelper.getObject(json, "result"))
				.getOrThrow(false, msg -> LoggerFactory.getLogger(ForgeroShapedRecipeSerializer.class).error("Error parsing forgero result: {}", msg));

		ItemStack outputStack = new ItemStack(Registries.ITEM.get(new Identifier(forgeroResult.item())));

		return new ForgeroShapedRecipe(id, group, category, width, height, ingredients, outputStack, forgeroKey, forgeroResult);
	}

	@Override
	public ForgeroShapedRecipe read(Identifier id, PacketByteBuf buf) {
		int width = buf.readVarInt();
		int height = buf.readVarInt();
		String group = buf.readString();
		CraftingRecipeCategory category = buf.readEnumConstant(CraftingRecipeCategory.class);
		DefaultedList<Ingredient> ingredients = DefaultedList.ofSize(width * height, Ingredient.EMPTY);
		for (int i = 0; i < ingredients.size(); ++i) {
			ingredients.set(i, Ingredient.fromPacket(buf));
		}
		ItemStack output = buf.readItemStack();

		Map<Character, RecipeIngredient> forgeroKey = buf.readMap(PacketByteBuf::readChar, b ->
				new RecipeIngredient(
						b.readOptional(PacketByteBuf::readString),
						b.readOptional(PacketByteBuf::readString)
				)
		);

		String resultItem = buf.readString();
		Optional<Map<String, String>> structure = buf.readOptional(b -> b.readMap(PacketByteBuf::readString, PacketByteBuf::readString));
		Optional<List<RecipeUpgrade>> upgrades = buf.readOptional(b -> b.readList(b2 -> new RecipeUpgrade(b2.readString(), b2.readString())));

		Optional<Map<String, List<?>>> properties = buf.readOptional(b -> {
			NbtCompound nbt = b.readNbt();
			if (nbt != null && nbt.contains("props")) {
				DataResult<Map<String, List<?>>> result = propertyDispatchCodec.codec()
						.parse(NbtOps.INSTANCE, nbt.get("props"));

				return result.getOrThrow(false, msg -> LoggerFactory.getLogger(ForgeroShapedRecipeSerializer.class).error("Error decoding properties from NBT: {}", msg));
			}
			return new HashMap<>();
		});

		RecipeOutput forgeroResult = new RecipeOutput(resultItem, structure, upgrades, properties);

		return new ForgeroShapedRecipe(id, group, category, width, height, ingredients, output, forgeroKey, forgeroResult);
	}

	private void writeRecipeIngredient(PacketByteBuf buf, RecipeIngredient ingredient) {
		buf.writeOptional(ingredient.item(), PacketByteBuf::writeString);
		buf.writeOptional(ingredient.tag(), PacketByteBuf::writeString);
	}

	@Override
	public void write(PacketByteBuf buf, ForgeroShapedRecipe recipe) {
		buf.writeVarInt(recipe.getWidth());
		buf.writeVarInt(recipe.getHeight());
		buf.writeString(recipe.getGroup());
		buf.writeEnumConstant(recipe.getCategory());

		// This loop iterates over vanilla Ingredients
		for (Ingredient ingredient : recipe.getIngredients()) {
			ingredient.write(buf);
		}
		buf.writeItemStack(recipe.getOutput(null));

		buf.writeMap(recipe.forgeroKey, (byteBuf, character) -> byteBuf.writeChar(character), this::writeRecipeIngredient);

		RecipeOutput result = recipe.forgeroResult;
		buf.writeString(result.item());
		buf.writeOptional(result.structure(), (b, map) -> b.writeMap(map, PacketByteBuf::writeString, PacketByteBuf::writeString));
		buf.writeOptional(result.upgrades(), (b, list) -> b.writeCollection(list, (b2, upgrade) -> {
			b2.writeString(upgrade.slot());
			b2.writeString(upgrade.component());
		}));

		buf.writeOptional(result.properties(), (b, props) -> {
			DataResult<NbtElement> nbtResult = propertyDispatchCodec.codec().encodeStart(NbtOps.INSTANCE, props);
			NbtCompound nbt = new NbtCompound();
			nbt.put("props", nbtResult.getOrThrow(false, msg -> LoggerFactory.getLogger(ForgeroShapedRecipeSerializer.class).error("Error encoding properties to NBT: {}", msg)));
			b.writeNbt(nbt);
		});
	}
}
