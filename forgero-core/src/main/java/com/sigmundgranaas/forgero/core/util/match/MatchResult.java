package com.sigmundgranaas.forgero.core.util.match;

public interface MatchResult {
	boolean match();

	boolean fail();

	public int matchGrade();
}
