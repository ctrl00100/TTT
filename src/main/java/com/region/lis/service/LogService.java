package com.region.lis.service;

import com.region.lis.entity.BizLog;
import com.region.lis.mapper.BizLogMapper;
import com.region.lis.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogService {

    @Autowired
    private BizLogMapper bizLogMapper;

    public void record(String bizType, String orderNo, String reportId,
                       String operator, String content, String ip) {

        // 如果 ip 为空，自动取当前请求 IP
        if (ip == null || ip.isEmpty()) {
            ip = RequestUtil.getIp();
        }
        BizLog log = new BizLog();
        log.setBizType(bizType);
        log.setOrderNo(orderNo);
        log.setReportId(reportId);
        log.setOperator(operator);
        log.setContent(content);
        log.setIp(ip);
        bizLogMapper.insert(log);
    }
}