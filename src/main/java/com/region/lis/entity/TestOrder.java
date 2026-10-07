package com.region.lis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("test_order")
public class TestOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private String patientName;
    private String idCard;        // 新增：身份证
    private String phone;         // 新增：手机号
    private String townshipId;
    private String itemCode;
    private String countyCode;
    private String  reportId;        // 新增：关联报告ID

    private Integer status;
    private Integer retryCount;
    private LocalDateTime createTime;
}