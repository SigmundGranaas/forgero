package com.sigmundgranaas.forgero.core.util.match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sigmundgranaas.forgero.core.texture.utils.Offset;

public class MatchContext implements Matchable {
	private final List<Matchable> matches;
	private final Map<String, Object> metatata;

	public MatchContext() {
		this.matches = new ArrayList<>();
		this.metatata = new HashMap<>();
	}

	public MatchContext(List<Matchable> matchable) {
		this.matches = new ArrayList<>(matchable);
		this.metatata = new HashMap<>();
	}

	public static MatchContext of() {
		return new MatchContext();
	}

	public static MatchContext of(List<Matchable> matchable) {
		return new MatchContext(matchable);
	}

	public MatchContext add(Matchable matchable) {
		if (!matches.contains(matchable)) {
			this.matches.add(matchable);
		}
		return this;
	}

	@Override
	public boolean test(Matchable match, MatchContext context) {
		return matches.stream().anyMatch(matchable -> matchable.test(match, context));
	}

	public Map<String, Object> metaData() {
		return this.metatata;
	}

	public void applyMetadata(String id, Object data) {
		if (this.metatata.containsKey(id)) {
			if (this.metatata.get(id) instanceof Offset offset && data instanceof Offset offset1) {
				this.metatata.put(id, offset1.apply(offset));
			}
		}
	}
}
