package com.ayor.service.impl;

import com.ayor.entity.app.vo.ConversationVO;
import com.ayor.entity.app.vo.UserInfoVO;
import com.ayor.entity.pojo.Account;
import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.stomp.ChatUnread;
import com.ayor.mapper.AccountMapper;
import com.ayor.mapper.ConversationMapper;
import com.ayor.service.ChatUnreadService;
import com.ayor.service.ConversationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    private final ChatUnreadService chatUnreadService;

    @Override
    @Cacheable(value = "conversation", key = "#result.conversationId", condition = "#accountId != null && #toAccountId != null")
    public ConversationVO getConversationByAccountId(Integer accountId, Integer toAccountId) {
        if (accountId == null) {
            return null;
        }
        Conversation conversation = this.lambdaQuery()
                .eq(Conversation::getAlphaAccountId, accountId)
                .eq(Conversation::getBetaAccountId, toAccountId)
                .or()
                .eq(Conversation::getAlphaAccountId, toAccountId)
                .eq(Conversation::getBetaAccountId, accountId)
                .one();
        if (conversation == null) {
            return null;
        }
        ConversationVO conversationVO = new ConversationVO();
        conversationVO.setConversationId(conversation.getConversationId());
        // 如果是发起者来查找对话
        if (accountId.equals(conversation.getAlphaAccountId()) ) {
            if(conversation.getHidden() == 1 ) {
                conversation.setHidden(0);
                this.updateById( conversation);
                return conversationVO;
            }
            if (conversation.getHidden() == 3) {
                conversation.setHidden(2);
                this.updateById( conversation);
                return conversationVO;
            }
            return conversationVO;
        }
        // 如果是接收者来查找对话
        if (accountId.equals(conversation.getBetaAccountId())) {
            if (conversation.getHidden() == 2) {
                conversation.setHidden(0);
                this.updateById( conversation);
                return conversationVO;
            }
            if (conversation.getHidden() == 3) {
                conversation.setHidden(1);
                this.updateById( conversation);
                return conversationVO;
            }
            return conversationVO;
        }
        return conversationVO;
    }

    @Override
    @CacheEvict(value = "conversation", key = "#conversationId", condition = "#accountId != null")
    public String hiddenConversation(Integer conversationId, Integer accountId) {
        Conversation conversation = this.getById(conversationId);
        if (accountId == null) {
            return "用户不存在";
        }
        if (conversation == null) {
            return "对话不存在";
        }
        if (accountId.equals(conversation.getAlphaAccountId())) {
            if (conversation.getHidden() == 0) {
                conversation.setHidden(1);
            }
            if (conversation.getHidden() == 2) {
                conversation.setHidden(3);
            }
            this.updateById(conversation);
            return null;
        }
        if (accountId.equals(conversation.getBetaAccountId())) {
            if (conversation.getHidden() == 0) {
                conversation.setHidden(2);
            }
            if (conversation.getHidden() == 1) {
                conversation.setHidden(3);
            }
            this.updateById(conversation);
            return null;
        }
        return "无权限";
    }

    @Override
    @CacheEvict(value = "conversationList", key = "#accountId", condition = "#accountId != null")
    public String createNewConversation(Integer accountId, String toUsername) {
        Account fromAccount = accountMapper.getAccountById(accountId);
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
    @Cacheable(value = "conversationList", key = "#accountId", condition = "#accountId != null", unless = "#result == null")
    public List<ConversationVO> getConversationList(Integer accountId) {
        Account account = accountMapper.getAccountById(accountId);
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
            if (conversation.getHidden() != 1 && conversation.getHidden() != 3) {
                Account accountById = accountMapper.getAccountById(conversation.getBetaAccountId());
                ConversationVO conversationVO = ConversationVO.builder()
                        .conversationId(conversation.getConversationId())
                        .userInfo(getUserInfoVO(accountById))
                        .updateTime(conversation.getUpdateTime())
                        .build();
                conversationVOs.add(conversationVO);
            }
        });
        reactiveConversations.forEach(conversation -> {
            if (conversation.getHidden() != 2 && conversation.getHidden() != 3) {
                Account accountById = accountMapper.getAccountById(conversation.getAlphaAccountId());
                ConversationVO conversationVO = ConversationVO.builder()
                        .conversationId(conversation.getConversationId())
                        .userInfo(getUserInfoVO(accountById))
                        .updateTime(conversation.getUpdateTime())
                        .build();
                conversationVOs.add(conversationVO);
            }
        });
        return conversationVOs;
    }

    @Override
    public List<ChatUnread> getUnreadList(Integer accountId) {
        Account account = accountMapper.getAccountById(accountId);
        List<Conversation> alphCconversationList = this.lambdaQuery()
                .eq(Conversation::getAlphaAccountId, account.getAccountId())
                .list();
        List<Conversation> betaCconversationList = this.lambdaQuery()
                .eq(Conversation::getBetaAccountId, account.getAccountId())
                .list();
        List<ChatUnread> chatUnreadList = new ArrayList<>();
        alphCconversationList.forEach(con -> {
            Long unread = chatUnreadService.getUnread(con.getConversationId(), accountId);
            chatUnreadList.add(ChatUnread.builder()
                    .conversationId(con.getConversationId())
                    .fromUserId(con.getBetaAccountId())
                    .unread(unread)
                    .build());
        });
        betaCconversationList.forEach(con -> {
            Long unread = chatUnreadService.getUnread(con.getConversationId(), accountId);
            chatUnreadList.add(ChatUnread.builder()
                    .conversationId(con.getConversationId())
                    .fromUserId(con.getAlphaAccountId())
                    .unread(unread)
                    .build());
        });
        return chatUnreadList;
    }

    @Override
    public String clearUnread(Integer conversationId, Integer fromUserId) {
        if (fromUserId == null) {
            return "无此用户";
        }
        Conversation conversation = this.getById(conversationId);
        if (conversation == null) {
            return "无此对话";
        }
        chatUnreadService.clearUnread(conversationId, fromUserId);
        return null;
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
