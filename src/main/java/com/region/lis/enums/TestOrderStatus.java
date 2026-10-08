package com.region.lis.enums;

/**
 * 检验医嘱状态机（区域检验核心流转状态）
 */
public enum TestOrderStatus {

    PENDING(0, "待推送县医院"),
    PUSHED(1, "已推送县医院，待检测"),
    TESTED(2, "已检测，待回传乡镇"),
    RETURNED(3, "已回传乡镇"),
    FAILED(4, "推送或检测失败"),
    CANCELLED(5, "已取消");

    private final int code;
    private final String desc;

    TestOrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }

    public static TestOrderStatus of(int code) {
        for (TestOrderStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知医嘱状态: " + code);
    }
}
