package com.ayor.service;

import com.ayor.entity.vo.UserProfileVO;
import com.ayor.entity.pojo.UserProfile;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户资料服务接口
 *
 * 管理用户的个人资料信息，支持默认资料初始化、查看个人和公开资料。
 *
 * 主要功能:
 * - 初始化默认资料
 * - 创建默认资料
 * - 查看个人资料
 * - 查看公开资料（含隐私校验）
 *
 * @see UserProfile 用户资料实体
 * @see UserProfileVO 用户资料视图对象
 * @author ayor
 * @since 1.0.0
 */
public interface UserProfileService extends IService<UserProfile> {

    UserProfile initDefaultIfAbsent(Integer accountId);

    UserProfile createDefault(Integer accountId);

    UserProfileVO getMyProfile(Integer accountId);

    UserProfileVO getPublicProfile(Integer viewerId, Integer accountId);
}
