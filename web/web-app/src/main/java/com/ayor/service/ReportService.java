package com.ayor.service;

import com.ayor.entity.dto.ContentReportDTO;
import com.ayor.entity.dto.UserReportDTO;

public interface ReportService {

    String createUserReport(Integer reporterAccountId, Integer reportedAccountId, UserReportDTO dto);

    String createThreadReport(Integer reporterAccountId, Integer threadId, ContentReportDTO dto);

    String createPostReport(Integer reporterAccountId, Integer postId, ContentReportDTO dto);
}
