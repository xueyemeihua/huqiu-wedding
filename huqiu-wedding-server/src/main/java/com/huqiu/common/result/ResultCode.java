package com.huqiu.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    PARAM_ERROR(400, "参数错误"),
    STOCK_NOT_ENOUGH(4001, "可用库存不足"),
    STOCK_CONFLICT(4002, "库存更新冲突，请重试");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}