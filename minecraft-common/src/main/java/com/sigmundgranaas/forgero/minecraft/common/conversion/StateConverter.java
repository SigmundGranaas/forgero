package com.sigmundgranaas.forgero.minecraft.common.conversion;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.minecraft.common.utils.ItemUtils;
import com.sigmundgranaas.forgero.minecraft.common.utils.StateUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.apache.http.MethodNotSupportedException;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public interface StateConverter {
    Cache<ItemStack, Optional<State>> stackCache = CacheBuilder.newBuilder()
            .maximumSize(600)
            .expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
            .build(new CacheLoader<>() {
                @Override
                public @NotNull Optional<State> load(@NotNull ItemStack key) throws Exception {
                    throw new MethodNotSupportedException("Don't use the load method directly");
                }
            });

    static Optional<State> of(ItemStack stack) {
        try {
            return stackCache.get(stack, () -> new StackToItemConverter().convert(stack));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    static Optional<State> of(Item item) {
        return new ItemToStateConverter(ItemUtils::itemToStateFinder).convert(item);
    }

    static ItemStack of(State state) {
        return new StateToStackConverter(ItemUtils::itemFinder, StateUtils::containerMapper).convert(state);
    }
}
