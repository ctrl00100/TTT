package com.region.lis.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.region.lis.entity.TestItemMapping;
import com.region.lis.entity.TestOrder;
import com.region.lis.entity.TestResult;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestItemMappingMapper extends BaseMapper<TestItemMapping> {
}

