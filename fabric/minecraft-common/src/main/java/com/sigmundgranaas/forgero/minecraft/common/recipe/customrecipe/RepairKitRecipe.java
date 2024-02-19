package com.sigmundgranaas.forgero.minecraft.common.recipe.customrecipe;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.FORGERO_IDENTIFIER;

import java.util.Optional;
import java.util.stream.IntStream;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.condition.Conditions;
import com.sigmundgranaas.forgero.core.property.v2.ComputedAttribute;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedTool;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompositeEncoder;
import com.sigmundgranaas.forgero.minecraft.common.recipe.ForgeroRecipeSerializer;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import org.apache.commons.lang3.NotImplementedException;

public class RepairKitRecipe extends ShapelessRecipe {
	private final StateService service;

	public RepairKitRecipe(ShapelessRecipe recipe, StateService service) {
		super(recipe.getGroup(),recipe.getCategory(), recipe.getResult(null), recipe.getIngredients());
		this.service = service;
	}

	@Override
	public boolean matches(RecipeInputInventory craftingInventory, World world) {
		return super.matches(craftingInventory, world) && IntStream.range(0, 8)
				.mapToObj(craftingInventory::getStack)
				.filter(stack -> service.convert(stack).isPresent())
				.anyMatch(stack -> stack.getDamage() > 0);
	}

	@Override
	public ItemStack craft(RecipeInputInventory craftingInventory, DynamicRegistryManager registryManager) {
		var state = IntStream.range(0, 8)
				.mapToObj(craftingInventory::getStack)
				.map(service::convert)
				.flatMap(Optional::stream)
				.findFirst();
		var originalStack = IntStream.range(0, 8)
				.mapToObj(craftingInventory::getStack)
				.filter(stack -> service.convert(stack).isPresent())
				.findFirst();
		if (state.isPresent() && originalStack.isPresent() && state.get() instanceof ConstructedTool tool) {
			var unbrokenState = tool.removeCondition(Conditions.BROKEN.name());
			int durability = ComputedAttribute.of(unbrokenState, Durability.KEY).asInt();
			var stack = originalStack.get();
			stack.getOrCreateNbt().put(FORGERO_IDENTIFIER, CompositeEncoder.ENCODER.encode(unbrokenState));
			var newDamage = stack.getDamage() - (durability / 3);
			var newStack = stack.copy();
			newStack.setDamage(Math.max(newDamage, 0));
			return newStack;
		}

		return getResult(registryManager).copy();
	}


	@Override
	public RecipeSerializer<?> getSerializer() {
		return RepairKitRecipeSerializer.INSTANCE;
	}

	public static class RepairKitRecipeSerializer implements ForgeroRecipeSerializer, RecipeSerializer<RepairKitRecipe> {
		private final ShapelessRecipe.Serializer rootSerializer = new ShapelessRecipe.Serializer();
		@Override
		public Codec<RepairKitRecipe> codec() {
			return CodecUtils.extendCodec(rootSerializer.codec(), (recipe) -> new RepairKitRecipe(recipe, StateService.INSTANCE), recipe -> recipe);
		}

		public static final RepairKitRecipeSerializer INSTANCE = new RepairKitRecipeSerializer();

		@Override
		public RecipeSerializer<?> getSerializer() {
			return INSTANCE;
		}

		@Override
		public RepairKitRecipe read(PacketByteBuf packetByteBuf) {
			return new RepairKitRecipe(rootSerializer.read(packetByteBuf), StateService.INSTANCE);
		}

		@Override
		public void write(PacketByteBuf buf, RepairKitRecipe recipe) {
			rootSerializer.write(buf, recipe);
		}

		@Override
		public Identifier getIdentifier() {
			return new Identifier(Forgero.NAMESPACE, RecipeTypes.REPAIR_KIT_RECIPE.getName());
		}
	}
}
