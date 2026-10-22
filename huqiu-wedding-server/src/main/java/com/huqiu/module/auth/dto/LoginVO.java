package com.huqiu.module.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO {
    private String token;
    private Long userId;
    private Long merchantId;
    private String username;
    private String realName;
    private Integer role;
}