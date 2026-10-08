package com.region.lis.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_log")
public class BizLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String bizType;      // 申请接收/转发/接收报告/回写乡镇
    private String orderNo;
    private String reportId;
    private String operator;
    private String content;
    private String ip;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}