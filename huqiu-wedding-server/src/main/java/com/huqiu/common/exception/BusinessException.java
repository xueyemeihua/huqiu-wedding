package com.huqiu.common.exception;

import com.huqiu.common.result.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }

    public BusinessException(ResultCode code) {
        super(code.getMsg());
        this.code = code.getCode();
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }
}