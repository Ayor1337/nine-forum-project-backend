package com.ayor.type;

import lombok.Getter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Getter
public enum ThreadRankingPeriod {

    DAY("day"),

    WEEK("week"),

    MONTH("month");

    private final String value;

    ThreadRankingPeriod(String value) {
        this.value = value;
    }

    public Date getStartTime() {
        ZoneId zoneId = ZoneId.systemDefault();
        return switch (this) {
            case DAY -> Date.from(LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant());
            case WEEK -> Date.from(LocalDate.now(zoneId).minusDays(7).atStartOfDay(zoneId).toInstant());
            case MONTH -> Date.from(LocalDate.now(zoneId).minusDays(30).atStartOfDay(zoneId).toInstant());
        };
    }

    public static ThreadRankingPeriod fromValue(String value) {
        for (ThreadRankingPeriod period : values()) {
            if (period.value.equalsIgnoreCase(value)) {
                return period;
            }
        }
        return DAY;
    }
}
