package com.sigmundgranaas.forgero.core.util.match;

import java.util.List;
import java.util.Map;

public class MutableMatchContext extends MatchContext {
	public MutableMatchContext(Map<String, Object> metadata, List<Matchable> matches) {
		super(metadata, matches);
	}

	@Override
	public MatchContext put(String key, Object value) {
		this.metadata.put(key, value);
		return super.put(key, value);
	}

	@Override
	public MatchContext add(Matchable matchable) {
		if (!matches.contains(matchable)) {
			matches.add(matchable);
		}
		return super.add(matchable);
	}
}
