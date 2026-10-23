package com.inventorylite.common.persistence;

import java.util.Locale;

/** Builds lower-cased "contains" patterns for {@code lower(col) like :pattern escape '\'} queries. */
public final class LikePatterns {

    private LikePatterns() {
    }

    public static String contains(String text) {
        String escaped = text.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
