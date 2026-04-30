package com.ayor.mapper;

import com.ayor.entity.pojo.UserRelation;
import com.ayor.type.RelationStatus;
import com.ayor.type.RelationType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户关系数据访问层。
 */
public interface UserRelationMapper extends BaseMapper<UserRelation> {

    /**
     * 根据两个用户和关系类型查询关系记录。
     */
    UserRelation findRelation(@Param("fromAccountId") Integer fromAccountId,
                              @Param("toAccountId") Integer toAccountId,
                              @Param("relationType") RelationType relationType);

    /**
     * 判断指定方向的关系是否处于给定状态。
     */
    boolean existsRelation(@Param("fromAccountId") Integer fromAccountId,
                           @Param("toAccountId") Integer toAccountId,
                           @Param("relationType") RelationType relationType,
                           @Param("status") RelationStatus status);

    /**
     * 判断两个用户之间是否存在任一方向的拉黑关系。
     */
    boolean existsBlockedEitherDirection(@Param("firstAccountId") Integer firstAccountId,
                                         @Param("secondAccountId") Integer secondAccountId);
}
