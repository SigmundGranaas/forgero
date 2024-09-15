package com.sigmundgranaas.forgero.core.resource.data.processor;

import com.sigmundgranaas.forgero.core.resource.data.v2.data.IngredientData;

import java.util.Collections;
import java.util.List;

public class IngredientCollection {
	private final int totalSize;
	private List<IngredientInflater> collection;

	public IngredientCollection(int totalSize) {
		this.totalSize = totalSize;
		this.collection = Collections.emptyList();
	}

	public void addEntries(int index, List<IngredientData> data) {
		if (collection.isEmpty()) {
			this.collection = data.stream().map(entry -> new IngredientInflater(totalSize, List.of(entry))).toList();
		} else {
			this.collection = collection.stream().map(entry -> entry.addEntries(index, data)).flatMap(List::stream).toList();
		}
	}

	public List<IngredientInflater> getCollection() {
		return collection.stream().distinct().toList();
	}
}
