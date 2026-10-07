package com.region.lis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.region.lis.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}