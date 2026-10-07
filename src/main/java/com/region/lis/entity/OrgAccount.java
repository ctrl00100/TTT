package com.region.lis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;        // 新增
    private LocalDateTime passwordUpdateTime;   // 新增
    private Integer failCount;                  // 新增
}