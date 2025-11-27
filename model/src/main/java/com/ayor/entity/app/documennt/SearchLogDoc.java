package com.ayor.entity.app.documennt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "search_log")
public class SearchLogDoc {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String keyword;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Date)
    private Instant ts;
}
