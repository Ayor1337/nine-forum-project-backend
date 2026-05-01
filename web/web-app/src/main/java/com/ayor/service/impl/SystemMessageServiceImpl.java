package com.ayor.service.impl;

import com.ayor.aspect.unread.MessageUnreadNotif;
import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.SystemMessageVO;
import com.ayor.entity.pojo.SystemMessage;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.SystemMessageMapper;
import com.ayor.service.SystemMessageService;
import com.ayor.type.UnreadMessageType;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SystemMessageServiceImpl extends ServiceImpl<SystemMessageMapper, SystemMessage> implements SystemMessageService {

    @Override
    @MessageUnreadNotif(
            accountId = "#accountId",
            subscribeDest = "/notif/system",
            type = UnreadMessageType.SYSTEM_MESSAGE,
            doRead = true
    )
    /**
     * 分页获取用户的系统消息，并在读取后清空对应未读数。
     */
    public PageEntity<SystemMessageVO> listSystemMessage(Integer pageNum, Integer pageSize, Integer accountId) {
        if (accountId == null) {
            return null;
        }
        if (pageNum == null || pageNum < 1) {
            return null;
        }
        Page<SystemMessage> page = this.lambdaQuery()
                .eq(SystemMessage::getAccountId, accountId)
                .or()
                .eq(SystemMessage::getAccountId, null)
                .orderByDesc(SystemMessage::getCreateTime)
                .page(Page.of(pageNum, pageSize));
        return new PageEntity<>(page.getTotal(), toVOList(page.getRecords()));
    }
    /**
     * 将系统消息实体列表转换为视图对象列表。
     */


    private List<SystemMessageVO> toVOList(List<SystemMessage> systemMessageList) {
        List<SystemMessageVO> systemMessageVOList = new ArrayList<>();
        systemMessageList.forEach(systemMessage -> {
            SystemMessageVO systemMessageVO = new SystemMessageVO();
            BeanUtils.copyProperties(systemMessage, systemMessageVO);
            systemMessageVOList.add(systemMessageVO);
        });
        return systemMessageVOList;
    }


}
