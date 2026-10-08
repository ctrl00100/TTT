package com.region.lis.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("test_result")
public class TestResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String resultValue;
    // 状态枚举：TestResultStatus.PENDING/RETURNED/CONFIRMED
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private Integer isCritical;       // 新增：是否危机值 0否 1是
    private String criticalLevel;     // 新增：危机程度
    private String hrFlag;        // 新增：互认标识
    private String hrReason;      // 新增：不互认原因
}