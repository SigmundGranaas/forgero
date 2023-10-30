package com.sigmundgranaas.forgero.fabric.mixins;

import static com.sigmundgranaas.forgero.core.identifier.Common.ELEMENT_SEPARATOR;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.core.configuration.ForgeroConfigurationLoader;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Mixin(LootTable.class)
public class DisableVanillaToolsLootTableGeneration {


	private static final List<String> vanillaMaterials = List.of("wooden", "stone", "iron", "golden", "diamond", "netherite");

	private static final List<String> vanillaTools = List.of("pickaxe", "shovel", "axe", "sword", "hoe");

	private static final Set<Item> vanillaToolSet = vanillaMaterials.stream()
			.flatMap(material -> vanillaTools.stream().map(tool -> new Identifier(material + "_" + tool)))
			.map(Registry.ITEM::getOrEmpty)
			.flatMap(Optional::stream)
			.collect(Collectors.toSet());


	@Inject(method = "generateLoot(Lnet/minecraft/loot/context/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"), cancellable = true)
	public void forgero$mapToForgeroLootOrDisableVanillaLoot(LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
		ObjectArrayList<ItemStack> stacks = cir.getReturnValue();
		if (ForgeroConfigurationLoader.configuration.convertVanillaToolLoot) {
			stacks = stacks.stream()
					.map(this::processStack)
					.collect(ObjectArrayList.toList());
		}
		if (ForgeroConfigurationLoader.configuration.disableVanillaLoot || ForgeroConfigurationLoader.configuration.disableVanillaTools) {
			stacks = stacks.stream()
					.filter(stack -> !vanillaToolSet.contains(stack.getItem()))
					.collect(ObjectArrayList.toList());
		}
		cir.setReturnValue(stacks);
	}

	private ItemStack processStack(ItemStack stack) {
		if (vanillaToolSet.contains(stack.getItem())) {
			String newId = resultItemRenamer(Registry.ITEM.getId(stack.getItem()).toString());
			Item newItem = Registry.ITEM.get(new Identifier(newId));
			ItemStack newStack = new ItemStack(newItem);

			if (stack.hasNbt()) {
				newStack.setNbt(stack.copy().getOrCreateNbt());
			}
			return newStack;
		}
		return stack;
	}


	private String resultItemRenamer(String item) {
		String result = item;

		if (result.contains("minecraft")) {
			result = result.replace("minecraft", Forgero.NAMESPACE);
		}
		if (result.contains("golden")) {
			result = result.replace("golden", "gold");
		}
		if (result.contains("wooden")) {
			result = result.replace("wooden", "oak");
		}
		if (result.contains("_")) {
			result = result.replace("_", ELEMENT_SEPARATOR);
		}
		return result;
	}

}
