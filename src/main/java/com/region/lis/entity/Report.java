package com.region.lis.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report")
public class Report {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String reportId;        // 报告编号
    private String orderNos;        // 关联医嘱号（逗号分隔，合管检验）
    private String patientName;
    private String townshipId;
    private String reportImageUrl;  // 报告图片地址
    // 状态枚举：ReportStatus.PENDING/RETURNED
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}