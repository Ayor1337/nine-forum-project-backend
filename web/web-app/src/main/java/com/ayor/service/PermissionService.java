package com.ayor.service;

import com.ayor.entity.pojo.Permission;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 权限服务接口
 *
 * 继承自MyBatis-Plus的IService接口,提供标准CRUD操作。
 * 当前无自定义业务方法,使用MyBatis-Plus默认实现。
 *
 * @see IService MyBatis-Plus基础服务接口
 * @see Permission 权限实体
 * @author ayor
 * @since 1.0.0
 */
public interface PermissionService extends IService<Permission> {

}
