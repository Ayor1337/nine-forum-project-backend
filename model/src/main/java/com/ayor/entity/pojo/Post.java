package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "post")
public class Post {

    @TableId(type = IdType.AUTO)
    @Id
    private Integer postId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Integer, index = false)
    private Integer accountId;

    @Field(type = FieldType.Date, index = false)
    private Date createTime;

    @Field(type = FieldType.Date, index = false)
    private Date updateTime;

    @Field(type = FieldType.Integer, index = false)
    private Integer threadId;

    @Field(type = FieldType.Integer, index = false)
    private Integer topicId;

    @Field(type = FieldType.Boolean, index = false)
    private Boolean isDeleted;

}
