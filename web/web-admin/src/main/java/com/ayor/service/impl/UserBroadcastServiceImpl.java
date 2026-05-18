package com.ayor.service.impl;

import com.ayor.entity.dto.UserBroadcastDTO;
import com.ayor.entity.message.UserSystemMessage;
import com.ayor.entity.pojo.Account;
import com.ayor.mapper.AccountMapper;
import com.ayor.service.UserBroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBroadcastServiceImpl implements UserBroadcastService {

    private final AccountMapper accountMapper;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public String sendUserBroadcast(UserBroadcastDTO dto) {
        if (dto == null || dto.getAccountIds() == null || dto.getAccountIds().isEmpty()) {
            return "接收用户不能为空";
        }
        if (!StringUtils.hasText(dto.getTitle())) {
            return "通知标题不能为空";
        }
        if (!StringUtils.hasText(dto.getContent())) {
            return "通知内容不能为空";
        }
        List<Integer> accountIds = new LinkedHashSet<>(dto.getAccountIds()).stream().toList();
        for (Integer accountId : accountIds) {
            if (accountId == null || accountId <= 0) {
                return "存在无效用户：" + accountId;
            }
            Account account = accountMapper.selectById(accountId);
            if (account == null || account.isDeleted()) {
                return "存在无效用户：" + accountId;
            }
        }
        for (Integer accountId : accountIds) {
            rabbitTemplate.convertAndSend(
                    "broadcast.direct",
                    "broadcast",
                    new UserSystemMessage<>(dto.getContent().trim(), dto.getTitle().trim(), accountId));
        }
        return null;
    }
}
