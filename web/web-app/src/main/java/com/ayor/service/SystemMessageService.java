package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.SystemMessageVO;
import com.ayor.entity.pojo.SystemMessage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统消息服务接口
 *
 * 提供系统消息的查询功能,包括违规通知、系统公告等。
 *
 * 主要功能:
 * - 消息查询: 分页获取用户的系统消息列表
 *
 * @see SystemMessage 系统消息实体
 * @see SystemMessageVO 系统消息视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface SystemMessageService extends IService<SystemMessage> {

    /**
     * 获取用户的系统消息列表(分页)
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @param accountId 用户ID
     * @return 分页结果,包含系统消息视图对象列表
     */
    PageEntity<SystemMessageVO> listSystemMessage(Integer pageNum, Integer pageSize, Integer accountId);
}
