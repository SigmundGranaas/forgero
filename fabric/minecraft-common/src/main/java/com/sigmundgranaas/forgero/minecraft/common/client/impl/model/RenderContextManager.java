package com.sigmundgranaas.forgero.minecraft.common.client.impl.model;


import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.sigmundgranaas.forgero.minecraft.common.client.api.model.ContextAwareBakedModel;

import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RenderContextManager {
	private static final ThreadLocal<ContextAwareBakedModel.RenderContext> currentContext = new ThreadLocal<>();
	private static final Set<Item> blacklistedItems = new HashSet<>();
	private static final Set<Item> accessedItems = new HashSet<>();

	public static void setContext(ItemStack stack, ClientWorld world, LivingEntity entity, ModelTransformationMode mode, int seed) {
		if (!blacklistedItems.contains(stack.getItem())) {
			currentContext.set(new ContextAwareBakedModel.RenderContext(stack, world, entity, seed, Optional.ofNullable(mode)));
		}
	}

	public static void mutateContext(ItemStack stack, ModelTransformationMode mode) {
		ContextAwareBakedModel.RenderContext ctx = currentContext.get();
		if (!blacklistedItems.contains(stack.getItem()) && ctx != null) {
			currentContext.set(new ContextAwareBakedModel.RenderContext(stack, ctx.world(), ctx.entity(), ctx.seed(), Optional.of(mode)));}
	}

	public static Optional<ContextAwareBakedModel.RenderContext> getCurrentContext() {
		ContextAwareBakedModel.RenderContext context = currentContext.get();
		if (context != null) {
			accessedItems.add(context.stack().getItem());
		}
		return Optional.ofNullable(context);
	}

	public static void clearContext() {
		ContextAwareBakedModel.RenderContext context = currentContext.get();
		if (context == null) {
			return;
		}

		if (!accessedItems.contains(context.stack().getItem())) {
			// If context was set but not accessed, add to blacklist
			blacklistedItems.add(context.stack().getItem());
		}

		accessedItems.remove(context.stack().getItem());
		currentContext.remove();
	}
}
