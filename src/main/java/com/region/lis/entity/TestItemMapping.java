package com.region.lis.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("test_item_mapping")
public class TestItemMapping {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String countyCode;
    private String townshipCode;
    private String townshipId;
    private String itemName;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}