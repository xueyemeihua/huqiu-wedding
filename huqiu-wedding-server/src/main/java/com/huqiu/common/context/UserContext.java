package com.huqiu.common.context;

public class UserContext {
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static LoginUser require() {
        LoginUser u = HOLDER.get();
        if (u == null) {
            throw new com.huqiu.common.exception.BusinessException(
                    com.huqiu.common.result.ResultCode.UNAUTHORIZED);
        }
        return u;
    }

    public static void clear() {
        HOLDER.remove();
    }
}