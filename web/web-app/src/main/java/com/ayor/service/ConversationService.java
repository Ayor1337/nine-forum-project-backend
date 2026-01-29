package com.ayor.service;

import com.ayor.entity.app.vo.ConversationVO;
import com.ayor.entity.pojo.Conversation;
import com.ayor.entity.stomp.ChatUnread;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 私信对话服务接口
 *
 * 负责用户之间私信对话的管理,包括对话创建、查询、隐藏和未读状态管理。
 *
 * 主要功能:
 * - 对话管理: 创建、查询、隐藏私信对话
 * - 对话列表: 获取用户的所有对话
 * - 未读管理: 查询和清除未读消息标记
 *
 * 技术特性:
 * - 支持对话隐藏(不删除消息历史)
 * - 集成WebSocket实时消息通知
 * - 未读计数存储在Redis中
 *
 * @see Conversation 对话实体
 * @see ConversationVO 对话视图对象
 * @see ChatUnread 未读消息对象
 * @author ayor
 * @since 1.0.0
 */
public interface ConversationService extends IService<Conversation> {

    /**
     * 获取两个用户之间的对话
     * @param accountId 当前用户ID
     * @param toAccountId 对方用户ID
     * @return 对话视图对象,包含对话信息和最近消息;不存在时返回null
     */
    ConversationVO getConversationByAccountId(Integer accountId, Integer toAccountId);

    /**
     * 隐藏对话(不删除消息历史)
     *
     * 对话会从用户的对话列表中移除,但消息历史仍保留,
     * 当对方再次发送消息时对话会重新出现。
     *
     * @param conversationId 对话ID
     * @param accountId 操作用户ID,用于权限验证
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String hiddenConversation(Integer conversationId, Integer accountId);

    /**
     * 创建新的私信对话
     * @param accountId 当前用户ID
     * @param toUsername 对方用户名
     * @return 操作结果消息;成功返回null,失败返回错误描述(如对方用户不存在)
     */
    String createNewConversation(Integer accountId, String toUsername);

    /**
     * 获取用户的所有对话列表
     * @param accountId 用户ID
     * @return 对话视图对象列表,按最后消息时间倒序排列
     */
    List<ConversationVO> getConversationList(Integer accountId);

    /**
     * 获取用户的未读消息列表
     * @param accountId 用户ID
     * @return 未读消息对象列表,包含每个对话的未读数量
     */
    List<ChatUnread> getUnreadList(Integer accountId);

    /**
     * 清除对话的未读标记
     * @param conversationId 对话ID
     * @param fromUserId 对方用户ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     * @note 会更新Redis中的未读计数
     */
    String clearUnread(Integer conversationId, Integer fromUserId);
}
