package com.splitwise.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class MoneyUtils {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    private MoneyUtils() {}

    public static BigDecimal round(BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
        return amount.setScale(SCALE, ROUNDING_MODE);
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
    }

    public static boolean isZero(BigDecimal amount) {
        if (amount == null) return true;
        return round(amount).compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean equals(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return round(a).compareTo(round(b)) == 0;
    }

    public static BigDecimal sum(List<BigDecimal> amounts) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal amount : amounts) {
            if (amount != null) {
                total = total.add(amount);
            }
        }
        return round(total);
    }
}

