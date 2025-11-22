package com.ayor.entity.app.documennt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "thread")
public class ThreadDoc implements Serializable {

    @Serial
    private static final long serialVersionUID = 114L;

    @Id
    private String id;

    @Field(type = FieldType.Integer, index = false)
    private Integer threadId;

    @Field(type = FieldType.Integer, index = false)
    private Integer topicId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Integer, index = false)
    private Integer viewCount;

    @Field(type = FieldType.Integer, index = false)
    private Integer likeCount;

    @Field(type = FieldType.Integer, index = false)
    private Integer collectCount;

    @Field(type = FieldType.Date, index = false)
    private Date createTime;

    @Field(type = FieldType.Date, index = false)
    private Date updateTime;

    @Field(type = FieldType.Boolean, index = false)
    private Boolean isThreadTopic;

}
