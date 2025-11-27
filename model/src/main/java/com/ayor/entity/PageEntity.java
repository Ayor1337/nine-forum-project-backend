package com.ayor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageEntity<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1001L;

    private Long totalSize;

    private List<T> data;
}
