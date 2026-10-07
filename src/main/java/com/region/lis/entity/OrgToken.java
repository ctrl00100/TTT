package com.region.lis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("org_token")
public class OrgToken {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orgId;
    private String orgName;
    private String appKey;
    private String appSecret;
    private Integer status;
    private LocalDateTime createTime;
}