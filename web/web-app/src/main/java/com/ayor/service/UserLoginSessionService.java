package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.vo.LoginSessionVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;

/**
 * 登录会话管理服务接口
 *
 * 管理用户的登录会话记录，支持会话的创建、查询和撤销。
 *
 * 主要功能:
 * - 创建登录会话
 * - 查询会话列表
 * - 撤销指定会话
 * - 撤销账号全部会话
 * - 撤销当前会话
 *
 * @see LoginSessionVO 登录会话视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface UserLoginSessionService {

    void createSession(Integer accountId, String sessionId, String jwtId, Date expireTime, HttpServletRequest request);

    PageEntity<LoginSessionVO> listSessions(Integer accountId, String currentSessionId, Integer pageNum, Integer pageSize);

    String revokeSession(Integer accountId, String sessionId, String currentSessionId);

    void revokeAllSessions(Integer accountId);

    void revokeCurrentSession(String sessionId);
}
