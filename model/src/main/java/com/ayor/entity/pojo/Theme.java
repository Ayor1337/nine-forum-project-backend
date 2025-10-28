package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("db_theme")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Theme {

    @TableId(type = IdType.AUTO)
    private Integer themeId;

    private String title;

    private Boolean isDeleted;

}
