package com.ayor.entity.pojo;

import com.ayor.type.RelationStatus;
import com.ayor.type.RelationType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户关系实体，记录关注、拉黑等关系。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_relation")
public class UserRelation {

    @TableId(type = IdType.AUTO)
    private Long relationId;

    private Integer fromAccountId;

    private Integer toAccountId;

    private RelationType relationType;

    private RelationStatus status;

    private Date createTime;

    private Date updateTime;
}
