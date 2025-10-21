package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("db_topic_stat")
public class TopicStat {

    @TableId(type = IdType.AUTO)
    private Integer topicStatId;

    private Integer topicId;

    private Integer threadCount;

    private Integer viewCount;

}
