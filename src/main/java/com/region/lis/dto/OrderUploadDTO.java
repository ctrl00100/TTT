package com.region.lis.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderUploadDTO {
    private String orderNo;          // 医嘱号
    private String patientName;      // 患者姓名
    private String idCard;           // 身份证
    private String phone;            // 手机号
    private String townshipId;       // 乡镇机构ID
    private String itemCode;         // 乡镇项目编码
    private Integer quantity;        // 数量
}