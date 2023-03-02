package com.sigmundgranaas.forgero.minecraft.common.tooltip;

import com.sigmundgranaas.forgero.core.state.State;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedWriteHelper implements Writer {
	public static Map<Key, List<Text>> tooltipCache = new ConcurrentHashMap<>();
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
		if (tooltipCache.size() > 500) {
			tooltipCache.clear();
		}

		Key key = new Key(state, Screen.hasShiftDown(), Screen.hasControlDown());
		if (tooltipCache.containsKey(key)) {
			tooltip.addAll(tooltipCache.get(key));
		} else {
			var list = new ArrayList<Text>();
			writer.write(list, context);
			tooltipCache.put(key, list);
			tooltip.addAll(list);
		}
	}

	private record Key(State state, boolean hasShift, boolean hasCtrl) {
		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		@Override
		public String toString() {
			return String.format("%s%s%s", state.hashCode(), hasShift(), hasCtrl());
		}
	}
}
