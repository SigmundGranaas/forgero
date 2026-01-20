package com.sigmundgranaas.forgero.core.util.loader;

import java.io.InputStream;
import java.util.Optional;

public class ClassInputStreamLoader implements InputStreamLoader {
	@Override
	public Optional<InputStream> load(String location) {
		return Optional.ofNullable(this.getClass().getResourceAsStream(location));
	}
}
