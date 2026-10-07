package com.region.lis.util;

import org.apache.commons.lang3.StringUtils;

public class DesensitizeUtil {

    /**
     * 身份证脱敏：510***********1234
     */
    public static String idCard(String idCard) {
        if (StringUtils.isBlank(idCard) || idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 手机号脱敏：138****8000
     */
    public static String phone(String phone) {
        if (StringUtils.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 姓名脱敏：张*
     */
    public static String name(String name) {
        if (StringUtils.isBlank(name) || name.length() < 2) {
            return name;
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }
}