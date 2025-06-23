package com.sigmundgranaas.forgero.core.property.v2;


import java.util.Random;

public class FastRandomString {
		private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		private static final int STRING_LENGTH = 8;
		private static final Random random = new Random();

		public static String generateRandomString() {
			StringBuilder sb = new StringBuilder(STRING_LENGTH);
			for (int i = 0; i < STRING_LENGTH; i++) {
				sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
			}
			return sb.toString();
		}
}
