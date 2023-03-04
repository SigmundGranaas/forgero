package com.sigmundgranaas.forgero.core.util.match;

public class Match implements MatchResult {
	public static Match EXACT = of(1);
	public static Match CONSTRUCTED = of(3);
	public static Match RELATED = of(10);

	private final int grade;

	public Match(int grade) {
		this.grade = grade;
	}

	public static Match of(int matchGrade) {
		return new Match(matchGrade);
	}

	@Override
	public boolean match() {
		return true;
	}

	@Override
	public boolean fail() {
		return false;
	}

	@Override
	public int matchGrade() {
		return grade;
	}
}
