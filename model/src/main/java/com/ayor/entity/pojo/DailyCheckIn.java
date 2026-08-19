package com.ayor.entity.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("daily_check_in")
public class DailyCheckIn {

    @TableId(type = IdType.AUTO)
    private Long checkInId;

    private Integer accountId;

    private LocalDate checkInDate;

    private Date createTime;
}
