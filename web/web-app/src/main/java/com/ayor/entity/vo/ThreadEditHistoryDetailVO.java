package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThreadEditHistoryDetailVO extends ThreadEditHistoryVO {

    @Serial
    private static final long serialVersionUID = 116L;

    private String title;

    private String content;

}
