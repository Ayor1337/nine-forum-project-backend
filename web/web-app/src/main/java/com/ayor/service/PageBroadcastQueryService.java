package com.ayor.service;

import com.ayor.entity.vo.PageBroadcastVO;
import com.ayor.type.PageBroadcastScopeType;

import java.util.List;

/**
 * 页面广播查询服务接口
 *
 * 查询当前生效的页面级广播通知，按作用域（全局/分区）分类。
 *
 * 主要功能:
 * - 查询活动中的广播列表
 *
 * 技术特性:
 * - 基于 Redis 存储广播数据
 * - 支持按作用域和范围 ID 过滤
 *
 * @see PageBroadcastVO 页面广播视图对象
 * @see PageBroadcastScopeType 广播作用域类型
 * @author ayor
 * @since 1.0.0
 */
public interface PageBroadcastQueryService {

    List<PageBroadcastVO> listActiveBroadcasts(PageBroadcastScopeType scopeType, Integer scopeId);
}
