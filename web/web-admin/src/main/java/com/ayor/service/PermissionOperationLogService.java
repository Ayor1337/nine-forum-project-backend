package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.PermissionOperationLog;
import com.ayor.entity.vo.PermissionOperationLogVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PermissionOperationLogService extends IService<PermissionOperationLog> {

    PageEntity<PermissionOperationLogVO> listPermissionOperationLogs(Integer pageNum,
                                                                     Integer pageSize,
                                                                     String action,
                                                                     String username,
                                                                     String targetType,
                                                                     Long targetId,
                                                                     String sortOrder);

    List<String> listUsernameOptions();
}
