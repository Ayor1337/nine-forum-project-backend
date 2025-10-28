package com.ayor.service.impl;

import com.ayor.entity.app.vo.ConversationVO;
import com.ayor.entity.app.vo.UserInfoVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Conversation;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.service.ConversationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    private final AccountMapper accountMapper;

    @Override
    public String createNewConversation(String username, String toUsername) {
        Account fromAccount = accountMapper.getAccountByUsername(username);
        if (fromAccount == null) {
            return "发送用户不存在";
        }
        Account toAccount = accountMapper.getAccountByUsername(toUsername);
        if (toAccount == null) {
            return "接收用户不存在";
        }
        boolean isConversationExists = this.lambdaQuery()
                .eq(Conversation::getAlphaAccountId, fromAccount.getAccountId())
                .eq(Conversation::getBetaAccountId, toAccount.getAccountId())
                .or()
                .eq(Conversation::getAlphaAccountId, toAccount.getAccountId())
                .eq(Conversation::getBetaAccountId, fromAccount.getAccountId())
                .exists();
        if (isConversationExists) {
            return "已存在对话";
        }
        Conversation conversation = new Conversation();
        conversation.setCreateTime(new Date());
        conversation.setAlphaAccountId(fromAccount.getAccountId());
        conversation.setBetaAccountId(toAccount.getAccountId());
        conversation.setUpdateTime(new Date());
        return save(conversation) ? null : "创建失败";
    }

    @Override
    public List<ConversationVO> getConversationList(String username) {
        Account account = accountMapper.getAccountByUsername(username);
        if (account == null) {
            return null;
        }
        List<ConversationVO> conversationVOs = new ArrayList<>();
        List<Conversation> initiativeConversations = this.lambdaQuery()
                .eq(Conversation::getAlphaAccountId, account.getAccountId())
                .list();
        List<Conversation> reactiveConversations = this.lambdaQuery()
                .eq(Conversation::getBetaAccountId, account.getAccountId())
                .list();

        initiativeConversations.forEach(conversation -> {
            Account accountById = accountMapper.getAccountById(conversation.getBetaAccountId());
            ConversationVO conversationVO = ConversationVO.builder()
                    .conversationId(conversation.getConversationId())
                    .userInfo(getUserInfoVO(accountById))
                    .updateTime(conversation.getUpdateTime())
                    .build();
            conversationVOs.add(conversationVO);
        });
        reactiveConversations.forEach(conversation -> {
            Account accountById = accountMapper.getAccountById(conversation.getAlphaAccountId());
            ConversationVO conversationVO = ConversationVO.builder()
                    .conversationId(conversation.getConversationId())
                    .userInfo(getUserInfoVO(accountById))
                    .updateTime(conversation.getUpdateTime())
                    .build();
            conversationVOs.add(conversationVO);
        });
        return conversationVOs;
    }

    private UserInfoVO getUserInfoVO(Account account) {
        if (account == null) {
            return null;
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(account, userInfoVO);
        return userInfoVO;
    }


}
