package com.sigmundgranaas.forgero.minecraft.common.match;

import javax.swing.text.html.parser.Entity;

import com.sigmundgranaas.forgero.core.util.match.ContextKey;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class MinecraftContextKeys {
	public static final ContextKey<Entity> ENTITY = ContextKey.of("entity", Entity.class);
	public static final ContextKey<World> WORLD = ContextKey.of("world", World.class);
	public static final ContextKey<ItemStack> STACK = ContextKey.of("stack", ItemStack.class);
}
