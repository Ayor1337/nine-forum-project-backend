package com.ayor.service.impl;

import com.ayor.entity.PageEntity;
import com.ayor.entity.pojo.PermissionOperationLog;
import com.ayor.entity.vo.PermissionOperationLogVO;
import com.ayor.mapper.PermissionOperationLogMapper;
import com.ayor.service.PermissionOperationLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class PermissionOperationLogServiceImpl
        extends ServiceImpl<PermissionOperationLogMapper, PermissionOperationLog>
        implements PermissionOperationLogService {

    private static final int DEFAULT_PAGE_NUM = 1;

    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    public PageEntity<PermissionOperationLogVO> listPermissionOperationLogs(Integer pageNum,
                                                                           Integer pageSize,
                                                                           String action,
                                                                           String username,
                                                                           String targetType,
                                                                           Long targetId,
                                                                           String sortOrder) {
        int normalizedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        int offset = (normalizedPageNum - 1) * normalizedPageSize;
        String normalizedAction = StringUtils.hasText(action) ? action : null;
        String normalizedUsername = StringUtils.hasText(username) ? username : null;
        String normalizedTargetType = StringUtils.hasText(targetType) ? targetType : null;
        boolean sortAsc = "asc".equalsIgnoreCase(sortOrder);
        return new PageEntity<>(
                baseMapper.countPermissionOperationLogs(normalizedAction, normalizedUsername, normalizedTargetType, targetId),
                baseMapper.selectPermissionOperationLogs(
                        offset,
                        normalizedPageSize,
                        normalizedAction,
                        normalizedUsername,
                        normalizedTargetType,
                        targetId,
                        sortAsc));
    }

    @Override
    public List<String> listUsernameOptions() {
        return baseMapper.selectUsernameOptions();
    }
}
