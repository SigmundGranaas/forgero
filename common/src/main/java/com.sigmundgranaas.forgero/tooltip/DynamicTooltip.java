package com.sigmundgranaas.forgero.tooltip;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.text.Text;


public class DynamicTooltip {
	public static final List<Text> EMPTY = new ArrayList<>();
	private volatile List<Text> currentTooltip;

	public DynamicTooltip() {
		this.currentTooltip = EMPTY;
	}

	public void updateTooltip(List<Text> newTooltip) {
		this.currentTooltip = newTooltip;
	}

	public List<Text> getTooltip() {
		return currentTooltip;
	}
}
