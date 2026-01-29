package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.app.document.ThreadDoc;
import com.ayor.entity.app.dto.PostDTO;
import com.ayor.entity.app.vo.PostVO;
import com.ayor.entity.app.vo.ReplyMessageVO;
import com.ayor.entity.pojo.Post;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 评论(Post)业务服务接口
 *
 * 负责帖子评论的核心业务逻辑,包括评论的查询、发布、删除和消息通知。
 *
 * 主要功能:
 * - 评论查询: 按帖子ID获取评论列表
 * - 评论管理: 发布、删除评论
 * - 消息通知: 回复消息列表查询,集成WebSocket实时通知
 * - 数据转换: Post到ThreadDoc的转换(用于搜索)
 *
 * 技术特性:
 * - 发布评论时触发WebSocket消息推送
 * - 支持@提及用户功能
 * - 评论内容支持富文本(Quill格式)
 *
 * @see Post 评论实体
 * @see PostVO 评论视图对象
 * @see ReplyMessageVO 回复消息视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface PostService extends IService<Post> {

    /**
     * 获取帖子的所有评论
     * @param threadId 帖子ID
     * @return 评论视图对象列表,包含评论内容、作者信息、点赞数等
     */
    List<PostVO> getPostsByThreadId(Integer threadId);

    /**
     * 发布新评论
     *
     * 发布成功后会通过WebSocket向被回复用户推送未读消息通知。
     *
     * @param postDTO 评论数据传输对象,包含内容、所属帖子ID、回复对象等
     * @param userId 发布评论的用户ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     * @note 会触发WebSocket消息推送给被回复用户
     */
    String insertPost(PostDTO postDTO, Integer userId);

    /**
     * 删除评论(用户操作,逻辑删除)
     *
     * 用户只能删除自己发布的评论。
     *
     * @param postId 评论ID
     * @param userId 操作用户ID,用于权限验证
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String removePostAuthorizeAccountId(Integer postId, Integer userId);

    /**
     * 删除评论(管理员操作,物理删除)
     * @param postId 评论ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String removePostPermission(Integer postId);

    /**
     * 获取用户收到的回复消息列表(分页)
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @param accountId 用户ID
     * @return 分页结果,包含回复消息视图对象列表
     */
    PageEntity<ReplyMessageVO> listReplyMessage(Integer pageNum, Integer pageSize, Integer accountId);

    /**
     * 将评论列表转换为Elasticsearch文档列表
     *
     * 用于将评论数据同步到Elasticsearch,使评论内容也可被搜索。
     *
     * @param posts 评论实体列表
     * @return Elasticsearch文档对象列表
     * @see ThreadDoc 文档中会包含评论内容
     */
    List<ThreadDoc> toThreadDoc(List<Post> posts);
}
