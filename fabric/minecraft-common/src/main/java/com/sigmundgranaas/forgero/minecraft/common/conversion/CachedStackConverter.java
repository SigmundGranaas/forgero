package com.sigmundgranaas.forgero.minecraft.common.conversion;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompoundParser;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants;
import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class CachedStackConverter implements Converter<ItemStack, Optional<State>> {
	private static final Cache<ItemStack, Optional<State>> stackCache = CacheBuilder.newBuilder()
			.maximumSize(600)
			.expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
			.build(new CacheLoader<>() {
				@Override
				public @NotNull
				Optional<State> load(@NotNull ItemStack key) {
					return Optional.empty();
				}
			});

	private static final LoadingCache<NbtCompound, Optional<State>> compoundCache = CacheBuilder.newBuilder()
			.maximumSize(600)
			.expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
			.build(new CacheLoader<>() {
				@Override
				public @NotNull
				Optional<State> load(@NotNull NbtCompound key) {
					return CompoundParser.STATE_PARSER.parse(key);
				}
			});
	private final StateService stateService;

	public CachedStackConverter(StateService stateService) {
		this.stateService = stateService;
	}

	@Override
	public Optional<State> convert(ItemStack stack) {
		try {
			return stackCache.get(stack, () -> convertCompound(stack));
		} catch (Exception e) {
			return stateService.find(stack.getItem());
		}
	}

	public Optional<State> convertCompound(ItemStack stack) {
		try {
			if (stack.hasNbt() && stack.getOrCreateNbt().contains(NbtConstants.FORGERO_IDENTIFIER)) {
				return compoundCache.getUnchecked(stack.getOrCreateNbt().getCompound(NbtConstants.FORGERO_IDENTIFIER));
			} else {
				return stateService.find(stack.getItem());
			}
		} catch (Exception e) {
			return Optional.empty();
		}
	}
}
