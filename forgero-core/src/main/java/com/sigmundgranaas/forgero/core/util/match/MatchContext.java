package com.sigmundgranaas.forgero.core.util.match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MatchContext implements Matchable {
	private final Map<String, Object> metadata;
	private final List<Matchable> matches;


	public MatchContext() {
		this.metadata = new HashMap<>();
		this.matches = new ArrayList<>();
	}

	public MatchContext(Map<String, Object> metadata, List<Matchable> matches) {
		this.metadata = new HashMap<>(metadata);
		this.matches = matches;
	}

	public MatchContext(List<Matchable> matches) {
		this.metadata = new HashMap<>();
		this.matches = matches;
	}


	public static MatchContext of() {
		return new MatchContext();
	}

	public static MatchContext of(List<Matchable> matches) {
		return new MatchContext(matches);
	}

	public MatchContext put(String key, Object value) {
		metadata.put(key, value);
		return new MatchContext(new HashMap<>(metadata), new ArrayList<>(matches));
	}

	public MatchContext add(Matchable matchable) {
		if (!matches.contains(matchable)) {
			this.matches.add(matchable);
		}
		return new MatchContext(new HashMap<>(metadata), new ArrayList<>(matches));
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		return matches.stream().anyMatch(matchable -> matchable.test(match, context));
	}


	public <T> Optional<T> get(String key, Class<T> type) {
		Object value = metadata.get(key);
		if (type.isInstance(value)) {
			return Optional.of(type.cast(value));
		}
		return Optional.empty();
	}


	public <T> Optional<T> get(ContextKey<T> key) {
		return get(key.getKey(), key.getClazz());
	}

	public MatchContext put(ContextKey<?> key, Object value) {
		return put(key.getKey(), value);
	}
}
