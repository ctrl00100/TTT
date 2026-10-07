package com.region.lis.common;

public class ResultCode {
    public static final int SUCCESS = 200;
    public static final int PARAM_ERROR = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int SYSTEM_ERROR = 500;

    // 业务码
    public static final int ITEM_NOT_MAPPED = 1001;   // 项目未对码
    public static final int ORDER_DUPLICATE = 1002;   // 医嘱重复上传
    public static final int REPORT_NOT_READY = 1003;  // 报告未出
    public static final int CRITICAL_VALUE = 1004;    // 危机值告警
}