package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ThemeTopicVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 112L;

    private Integer themeId;

    private String title;

    List<TopicVO> topics;

}
