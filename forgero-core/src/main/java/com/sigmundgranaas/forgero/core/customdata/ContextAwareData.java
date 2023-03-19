package com.sigmundgranaas.forgero.core.customdata;

public class ContextAwareData<T> {
	private final T value;
	private Context context = Context.TRANSITIVE;

	public ContextAwareData(Context context, T value) {
		this.context = context;
		this.value = value;
	}

	public Context context() {
		return context;
	}

	public T value() {
		return value;
	}
}
