package com.ayor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "分页数据对象")
public class PageEntity<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1001L;

    private Long totalSize;

    private List<T> data;
}
