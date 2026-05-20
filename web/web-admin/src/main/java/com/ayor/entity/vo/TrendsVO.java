package com.ayor.entity.vo;

import lombok.Data;

@Data
public class TrendsVO {

    private Integer todayPosts;

    private Float todayPostsRatio;

    private String todayPostsSummary;

    private Integer todayReplies;

    private Float todayRepliesRatio;

    private String todayRepliesSummary;

    private Integer todayReports;

    private Float todayReportsRatio;

    private String todayReportsSummary;
}
