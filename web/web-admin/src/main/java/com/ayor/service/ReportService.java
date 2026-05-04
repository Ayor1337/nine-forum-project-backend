package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.ReportHandleDTO;
import com.ayor.entity.message.ReportCreatedMessage;
import com.ayor.entity.pojo.Report;
import com.ayor.entity.vo.ReportVO;
import com.ayor.type.ReportStatus;
import com.ayor.type.ReportTargetType;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ReportService extends IService<Report> {

    void createFromMessage(ReportCreatedMessage message);

    PageEntity<ReportVO> getReports(Integer pageNum,
                                    Integer pageSize,
                                    ReportStatus status,
                                    ReportTargetType targetType,
                                    String reportType,
                                    Integer reporterAccountId,
                                    Integer reportedAccountId);

    ReportVO getReportDetail(Integer reportId);

    String handleReport(Integer reportId, Integer handlerAccountId, ReportHandleDTO dto);
}
