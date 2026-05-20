package com.ayor.entity.vo;

import lombok.Data;

@Data
public class KpiVO {

    private Integer totalUsers;

    private Float totalUsersDeltaPercent;

    private Integer todayActive;

    private Integer todayActivePeakHour;

    private Integer totalPosts;

    private Integer todayNewPosts;

    private Integer pendingReports;

    private Integer highPriorityReports;
}
