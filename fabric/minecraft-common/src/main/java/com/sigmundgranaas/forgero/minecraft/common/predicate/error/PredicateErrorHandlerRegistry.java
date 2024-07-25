package com.sigmundgranaas.forgero.minecraft.common.predicate.error;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class PredicateErrorHandlerRegistry implements Iterable<PredicateErrorHandler> {
	public static PredicateErrorHandlerRegistry INSTANCE = new PredicateErrorHandlerRegistry();

	private final List<PredicateErrorHandler> handlers = new ArrayList<>();

	public void registerHandler(PredicateErrorHandler handler) {
		handlers.add(handler);
	}

	@NotNull
	@Override
	public Iterator<PredicateErrorHandler> iterator() {
		return handlers.iterator();
	}
}
