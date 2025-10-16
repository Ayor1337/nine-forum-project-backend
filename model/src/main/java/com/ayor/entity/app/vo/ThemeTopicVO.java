package com.ayor.entity.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ThemeTopicVO {

    private Integer themeId;

    private String title;

    List<TopicVO> topics;

}
