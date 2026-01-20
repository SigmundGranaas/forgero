package com.sigmundgranaas.forgero.fabric.resources;

import java.io.InputStream;
import java.util.Optional;

public class ResourceLoadedFileService extends FileService {
	public ResourceLoadedFileService() {
	}

	public Optional<InputStream> getStream(String location) {
		var elements = location.split("/");
		var file = elements[elements.length - 1];
		var res = super.getStream("assets/forgero/textures/item/" + file);
		return res.or(() -> super.getStreamLogged(location));
	}

	@Override
	public Optional<InputStream> getStreamSilent(String location) {
		var elements = location.split("/");
		var file = elements[elements.length - 1];
		var res = super.getStreamSilent("assets/forgero/textures/item/" + file);
		return res.or(() -> super.getStreamSilent(location));
	}
}
