package com.region.lis.enums;

public enum ReportStatus {
    PENDING(0, "待回传乡镇"),
    RETURNED(1, "已回传乡镇");

    private final int code;
    private final String desc;

    ReportStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() { return code; }
    public String getDesc() { return desc; }
}
