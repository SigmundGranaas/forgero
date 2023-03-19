package com.sigmundgranaas.forgero.core.customdata;

/**
 * A data object that uses a context.
 * The context is used to determine whether the data is available in all resources that inherit from the resource it was declared in.
 * <p>
 * Will default to using transitively available data.
 *
 * @param <T> Any custom data
 */
public class ContextAwareData<T> {
	private final T value;
	@SuppressWarnings("UnusedAssignment")
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
