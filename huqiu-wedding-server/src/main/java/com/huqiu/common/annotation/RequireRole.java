package com.huqiu.common.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    /** 允许的角色：1商家管理员 2销售员 */
    int[] value();
}