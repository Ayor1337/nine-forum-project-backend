package com.ayor.type;

import lombok.Getter;

@Getter
public enum ThreadRankingMetric {

    LIKES("likes"),

    VIEWS("views"),

    COLLECTS("collects");

    private final String value;

    ThreadRankingMetric(String value) {
        this.value = value;
    }

    public static ThreadRankingMetric fromValue(String value) {
        for (ThreadRankingMetric metric : values()) {
            if (metric.value.equalsIgnoreCase(value)) {
                return metric;
            }
        }
        return LIKES;
    }
}
