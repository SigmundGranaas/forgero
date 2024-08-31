package com.sigmundgranaas.forgero.core.util;

import java.util.Comparator;
import java.util.Set;

public class StringSimilarity {
	private StringSimilarity() {
	}

	public static int calculateSimilarity(String s1, String s2) {
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();
		int[][] dp = new int[s1.length() + 1][s2.length() + 1];

		for (int i = 0; i <= s1.length(); i++) {
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0) {
					dp[i][j] = j;
				} else if (j == 0) {
					dp[i][j] = i;
				} else {
					dp[i][j] = Math.min(
							dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
							Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
					);
				}
			}
		}

		return dp[s1.length()][s2.length()];
	}

	public static String findClosestMatch(String inputKey, Set<String> candidates) {
		return candidates.stream()
				.min(Comparator.comparingInt(candidate -> calculateSimilarity(inputKey, candidate)))
				.orElse("");
	}
}
