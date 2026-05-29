package com.ayor.service;

import com.ayor.entity.vo.UserProfileVO;
import com.ayor.entity.pojo.UserProfile;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserProfileService extends IService<UserProfile> {

    UserProfile initDefaultIfAbsent(Integer accountId);

    UserProfile createDefault(Integer accountId);

    UserProfileVO getMyProfile(Integer accountId);

    UserProfileVO getPublicProfile(Integer viewerId, Integer accountId);
}
