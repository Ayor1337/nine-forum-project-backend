package com.ayor.service;

import com.ayor.entity.pojo.PermissionOperationLog;
import com.ayor.entity.vo.PermissionOperationLogVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface PermissionOperationLogService extends IService<PermissionOperationLog> {

    List<PermissionOperationLogVO> listPermissionOperationLogs();
}
