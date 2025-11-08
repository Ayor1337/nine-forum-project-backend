package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicStat {

    @TableId(type = IdType.AUTO)
    private Integer topicStatId;

    private Integer topicId;

    private Integer threadCount;

    private Integer viewCount;

}
