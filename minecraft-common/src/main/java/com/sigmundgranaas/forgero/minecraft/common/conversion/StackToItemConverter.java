package com.sigmundgranaas.forgero.minecraft.common.conversion;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompoundParser;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class StackToItemConverter implements Converter<ItemStack, Optional<State>> {
    private static final LoadingCache<NbtCompound, Optional<State>> compoundCache = CacheBuilder.newBuilder()
            .maximumSize(600)
            .expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
            .build(new CacheLoader<>() {
                @Override
                public @NotNull Optional<State> load(@NotNull NbtCompound key) throws Exception {
                    return CompoundParser.STATE_PARSER.parse(key);
                }
            });

    @Override
    public Optional<State> convert(ItemStack stack) {
        try {
            if (stack.hasNbt() && stack.getOrCreateNbt().contains(NbtConstants.FORGERO_IDENTIFIER)) {
                return compoundCache.getUnchecked(stack.getOrCreateNbt().getCompound(NbtConstants.FORGERO_IDENTIFIER));
            } else {
                return StateConverter.of(stack.getItem());
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
