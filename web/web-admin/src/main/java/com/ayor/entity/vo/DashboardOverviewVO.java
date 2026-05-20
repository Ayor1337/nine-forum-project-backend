package com.ayor.entity.vo;

import lombok.Data;

@Data
public class DashboardOverviewVO {

    private KpiVO kpi;

    private TrendsVO trends;

    private HealthVO health;
}
