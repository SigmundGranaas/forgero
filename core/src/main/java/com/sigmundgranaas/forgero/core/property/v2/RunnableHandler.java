package com.sigmundgranaas.forgero.core.property.v2;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

public interface RunnableHandler extends Runnable {
	RunnableHandler EMPTY = new RunnableHandler() {

		@Override
		public void run() {

		}

		@Override
		public String type() {
			return EMPTY_IDENTIFIER;
		}
	};

	String type();
}
