package com.sigmundgranaas.forgero.util;

public record MatchContext(String name) {
    public static MatchContext INGREDIENT = new MatchContext("INGREDIENT");
    public static MatchContext UPGRADE = new MatchContext("UPGRADE");
    public static MatchContext COMPOSITE = new MatchContext("COMPOSITE");
    public static MatchContext DEFAULT = new MatchContext("DEFAULT");
}
