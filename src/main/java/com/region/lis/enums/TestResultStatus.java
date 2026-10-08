package com.region.lis.enums;

public enum TestResultStatus {
    PENDING(0, "待回传"),
    RETURNED(1, "已回传"),
    CONFIRMED(2, "乡镇已确认接收");

    private final int code;
    private final String desc;

    TestResultStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }
}
