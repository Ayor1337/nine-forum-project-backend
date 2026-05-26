package com.ayor.service.impl;

import com.ayor.entity.pojo.PermissionOperationLog;
import com.ayor.entity.vo.PermissionOperationLogVO;
import com.ayor.mapper.PermissionOperationLogMapper;
import com.ayor.service.PermissionOperationLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PermissionOperationLogServiceImpl
        extends ServiceImpl<PermissionOperationLogMapper, PermissionOperationLog>
        implements PermissionOperationLogService {

    @Override
    public List<PermissionOperationLogVO> listPermissionOperationLogs() {
        return baseMapper.listLatest();
    }
}
