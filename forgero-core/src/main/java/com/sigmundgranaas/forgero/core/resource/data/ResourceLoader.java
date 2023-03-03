package com.sigmundgranaas.forgero.core.resource.data;

import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;

import java.util.List;
import java.util.Optional;


public interface ResourceLoader {
	List<DataResource> load();

	Optional<DataResource> loadResource(String path);
}
