package com.ayor.service;

import com.ayor.entity.PageEntity;
import com.ayor.entity.dto.AccountDTO;
import com.ayor.entity.vo.AccountVO;
import com.ayor.entity.pojo.Account;
import com.ayor.type.UserViolationType;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

/**
 * 管理员账户服务接口
 *
 * 提供后台用户管理功能,包括用户查询、编辑、删除、违规处理等管理员专用操作。
 *
 * 主要功能:
 * - 用户查询: 支持多条件筛选、搜索、分页
 * - 用户管理: 编辑资料、删除用户
 * - 违规处理: 标记用户违规并触发处理流程
 * - 下拉选项: 提供用户选择器数据源
 *
 * 权限要求:
 * - 所有方法需要管理员权限(ROLE_ADMIN)
 *
 * @see Account 用户实体
 * @see AccountVO 用户视图对象(管理员版)
 * @see UserViolationType 用户违规类型枚举
 * @author ayor
 * @since 1.0.0
 */
public interface AccountService extends UserDetailsService, IService<Account> {

    /**
     * 获取所有用户作为下拉选项(不分页)
     * @return 用户视图对象列表,用于下拉选择器
     */
    List<AccountVO> getAccountsAsSelectOptions();

    /**
     * 按关键词搜索用户作为下拉选项
     * @param query 搜索关键词,支持用户名、邮箱等字段模糊匹配
     * @return 匹配的用户视图对象列表
     */
    List<AccountVO> getAccountsAsSelectOptions(String query);

    /**
     * 获取用户详细信息
     * @param accountId 用户ID
     * @return 用户视图对象,包含完整的用户资料和角色信息
     */
    AccountVO getAccountById(Integer accountId);

    /**
     * 按角色ID获取用户列表(分页)
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @param roleId 角色ID
     * @return 分页结果,包含指定角色的所有用户
     */
    PageEntity<AccountVO> getAccountsByRoleId(Integer pageNum, Integer pageSize, Integer roleId);

    /**
     * 获取所有用户列表(分页)
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @return 分页结果,包含所有用户
     */
    PageEntity<AccountVO> getAccounts(Integer pageNum, Integer pageSize);

    /**
     * 按账户状态获取用户列表(分页)
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @param status 账户状态: 0=正常,1=禁用
     * @return 分页结果,包含指定状态的用户
     */
    PageEntity<AccountVO> getAccounts(Integer pageNum, Integer pageSize, Integer status);

    /**
     * 多条件搜索用户列表(分页)
     *
     * 支持按关键词搜索并按状态过滤。
     *
     * @param query 搜索关键词,支持用户名、邮箱等字段模糊匹配
     * @param pageNum 页码,从1开始
     * @param pageSize 每页记录数
     * @param status 账户状态: 0=正常,1=禁用;为null时不过滤
     * @return 分页结果,包含匹配条件的用户
     */
    PageEntity<AccountVO> getAccounts(String query, Integer pageNum, Integer pageSize, Integer status);

    /**
     * 标记用户违规并触发处理流程(管理员功能)
     *
     * 用于处理用户违规行为,系统会记录违规信息并通过RabbitMQ发送消息
     * 触发后续处理流程(如发送警告邮件、临时封禁等)。
     *
     * @param accountId 违规用户ID
     * @param type 违规类型,支持的值:
     *             - "spam" = 垃圾信息
     *             - "abuse" = 辱骂他人
     *             - "harassment" = 骚扰行为
     *             - "inappropriate" = 不当内容
     *             详见 UserViolationType 枚举类
     *
     * @return 操作结果消息;成功返回null,失败返回错误描述
     *
     * @see UserViolationType 完整的违规类型定义
     * @note 此操作会发送RabbitMQ消息到 "user.violation" 队列
     * @note 违规记录会持久化到 system_message 表
     */
    String violationProfile(Integer accountId, String type);

    /**
     * 更新用户信息
     * @param accountDTO 用户数据传输对象,包含要更新的字段
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String updateAccount(AccountDTO accountDTO);

    /**
     * 删除用户(逻辑删除)
     * @param accountId 用户ID
     * @return 操作结果消息;成功返回null,失败返回错误描述
     */
    String deleteAccount(Integer accountId);
}
