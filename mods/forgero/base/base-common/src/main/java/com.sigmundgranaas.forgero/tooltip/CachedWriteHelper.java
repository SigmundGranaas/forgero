package com.sigmundgranaas.forgero.tooltip;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sigmundgranaas.forgero.core.state.State;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

public class CachedWriteHelper implements Writer {
	private static final LoadingCache<Key, DynamicTooltip> tooltipCache = CacheBuilder.newBuilder()
			.expireAfterAccess(Duration.ofMinutes(1))
			.build(new CacheLoader<>() {
				@Override
				public @NotNull DynamicTooltip load(@NotNull Key key) {
					DynamicTooltip dt = new DynamicTooltip();
					CompletableFuture.runAsync(() -> {
						var list = new ArrayList<Text>();
						key.writer.write(list, key.ctx());
						dt.updateTooltip(list);
					});
					return dt;
				}
			});
	private final State state;
	private final Writer writer;

	public CachedWriteHelper(State state, Writer writer) {
		this.state = state;
		this.writer = writer;
	}

	public static CachedWriteHelper of(State state) {
		return new CachedWriteHelper(state, StateWriter.of(state));
	}

	public static CachedWriteHelper of(State state, Writer writer) {
		return new CachedWriteHelper(state, writer);
	}

	@Override
	public void write(List<Text> tooltip, TooltipContext context) {
		Key key = new Key(state, Screen.hasShiftDown(), Screen.hasControlDown(), context, writer);
		try {
			DynamicTooltip dynamicTooltip = tooltipCache.get(key);
			if (!dynamicTooltip.getTooltip().isEmpty()) {
				tooltip.addAll(dynamicTooltip.getTooltip());
			}
		} catch (ExecutionException ignored) {
		}
	}

	public record Key(State state, boolean hasShift, boolean hasCtrl, TooltipContext ctx, Writer writer) {
		@Override
		public int hashCode() {
			return Objects.hashCode(state.hashCode(), hasShift(), hasCtrl());
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Key key = (Key) o;
			return hasShift == key.hasShift && hasCtrl == key.hasCtrl && java.util.Objects.equals(state, key.state);
		}
	}
}
