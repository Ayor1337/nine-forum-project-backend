package com.ayor.service;

import com.ayor.entity.Base64Upload;
import com.ayor.entity.app.dto.AccountDTO;
import com.ayor.entity.app.dto.AccountProfileDTO;
import com.ayor.entity.app.dto.PasswordChangeDTO;
import com.ayor.entity.app.vo.UserInfoVO;
import com.ayor.entity.pojo.Account;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 用户账户服务接口
 *
 * 提供用户端账户管理功能,包括用户信息查询、头像/横幅更新、注册等操作。
 * 继承自Spring Security的UserDetailsService,用于用户认证。
 *
 * 主要功能:
 * - 用户信息: 获取用户资料、统计数据
 * - 图片上传: 更新用户头像和横幅图片
 * - 用户注册: 创建新账户
 *
 * 技术特性:
 * - 集成Spring Security认证机制
 * - 支持Base64格式的图片上传
 * - 图片存储使用MinIO对象存储
 *
 * @see UserInfoVO 用户信息视图对象
 * @see Account 用户实体
 * @see org.springframework.security.core.userdetails.UserDetailsService Spring Security用户详情服务
 * @author ayor
 * @since 1.0.0
 */
public interface AccountService extends UserDetailsService, IService<Account> {

    /**
     * 获取用户完整信息
     * @param accountId 用户ID
     * @return 用户信息视图对象,包含基本资料、统计数据(帖子数、关注数等)
     */
    UserInfoVO getUserInfo(Integer accountId);

    /**
     * 更新用户头像
     * @param accountId 用户ID
     * @param dto Base64格式的图片数据传输对象
     * @return 操作结果消息;成功返回null,失败返回错误描述
     * @note 图片会上传到MinIO并自动压缩优化
     */
    String updateUserAvatar(Integer accountId, Base64Upload dto);

    /**
     * 更新用户横幅(背景图)
     * @param accountId 用户ID
     * @param dto Base64格式的图片数据传输对象
     * @return 操作结果消息;成功返回null,失败返回错误描述
     * @note 图片会上传到MinIO并自动压缩优化
     */
    String updateUserBanner(Integer accountId, Base64Upload dto);

    /**
     * 注册新用户账户
     * @param accountDTO 账户数据传输对象,包含用户名、邮箱、密码等注册信息
     * @return 操作结果消息;成功返回null,失败返回错误描述(如用户名已存在)
     * @note 注册成功后会发送邮箱验证邮件
     */
    String insertNewAccount(AccountDTO accountDTO);

    String updateUserProfile(Integer accountId, AccountProfileDTO profileDTO);

    String updatePasswordWithOld(String token, PasswordChangeDTO pwDto);
}
