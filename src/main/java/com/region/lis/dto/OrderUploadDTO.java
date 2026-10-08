package com.region.lis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 乡镇卫生院上传检验医嘱 DTO
 */
@Data
public class OrderUploadDTO {

    @NotBlank(message = "医嘱号不能为空")
    @Size(max = 64, message = "医嘱号长度不能超过64")
    private String orderNo;

    @Size(max = 32, message = "患者姓名长度不能超过32")
    private String patientName;

    @Size(max = 18, message = "身份证号长度不能超过18")
    private String idCard;

    @Size(max = 20, message = "手机号长度不能超过20")
    private String phone;

    @NotBlank(message = "乡镇机构ID不能为空")
    @Size(max = 32, message = "机构ID长度不能超过32")
    private String townshipId;

    @NotBlank(message = "检验项目编码不能为空")
    @Size(max = 32, message = "项目编码长度不能超过32")
    private String itemCode;

    private Integer quantity;
}