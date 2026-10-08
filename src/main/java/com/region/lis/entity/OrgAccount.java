package com.region.lis.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("org_account")
public class OrgAccount {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orgCode;
    private String orgName;
    private String password;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;        // 新增
    private LocalDateTime passwordUpdateTime;   // 新增
    private Integer failCount;                  // 新增
}