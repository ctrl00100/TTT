package com.region.lis.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CountyLisClient {

    /**
     * 上传医嘱到县医院LIS
     */
    public boolean uploadOrder(String orderNo, String countyCode, String patientName) {
        log.info("📤 上传医嘱到县医院LIS：医嘱号={}, 县医院编码={}, 患者={}",
                orderNo, countyCode, patientName);

        // 模拟：编码以 XY 开头就成功
        if (countyCode != null && countyCode.startsWith("XY")) {
            log.info("✅ 县医院LIS接收成功");
            return true;
        }
        log.error("❌ 县医院LIS接收失败：编码不匹配");
        return false;
    }

    /**
     * 模拟县医院LIS出结果
     */
    public String getResult(String orderNo) {
        log.info("🔬 县医院LIS出结果：医嘱号={}", orderNo);
        return "结果正常";
    }
}