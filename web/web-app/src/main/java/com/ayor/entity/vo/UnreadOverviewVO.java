package com.ayor.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadOverviewVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 118L;

    private Long total;

    private Long reply;

    private Long mention;

    private Long system;

    private Long user;
}
