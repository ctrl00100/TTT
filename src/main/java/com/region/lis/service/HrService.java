package com.region.lis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HrService {

    /**
     * 判断互认标识
     * @param itemCode 项目编码
     * @param resultValue 结果值
     * @param isCritical 是否危机值
     * @return 互认标识
     */
    public String judgeHrFlag(String itemCode, String resultValue, Integer isCritical) {
        // 1. 危机值不互认
        if (isCritical != null && isCritical == 1) {
            return "NONE";
        }

        // 2. 县医院项目 → 区域HR
        if (itemCode != null && itemCode.startsWith("XY")) {
            return "REGION_HR";
        }

        // 3. 其他 → 不互认
        return "NONE";
    }

    /**
     * 获取不互认原因
     */
    public String getHrReason(String hrFlag, Integer isCritical) {
        if ("NONE".equals(hrFlag)) {
            if (isCritical != null && isCritical == 1) {
                return "危机值不互认";
            }
            return "项目未纳入互认范围";
        }
        return null;
    }
}