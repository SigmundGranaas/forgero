package com.sigmundgranaas.forgero.core.util.loader;

import java.io.InputStream;
import java.util.Optional;

public class SwitchableMultiLoader implements InputStreamLoader {
	private InputStreamLoader loader;

	public SwitchableMultiLoader() {
		this.loader = new ClassLoader();
	}

	@Override
	public Optional<InputStream> load(String location) {
		var res = loader.load(location);
		if (res.isPresent()) {
			return res;
		} else {
			if (loader instanceof ClassLoader) {
				return tryWithLoader(location, new ClassInputStreamLoader());
			} else if (loader instanceof ClassInputStreamLoader) {
				return tryWithLoader(location, new ClassLoader());
			}
		}
		return Optional.empty();
	}

	private Optional<InputStream> tryWithLoader(String location, InputStreamLoader newLoader) {
		var res = newLoader.load(location);
		if (res.isPresent()) {
			this.loader = newLoader;
		}
		return res;
	}
}
