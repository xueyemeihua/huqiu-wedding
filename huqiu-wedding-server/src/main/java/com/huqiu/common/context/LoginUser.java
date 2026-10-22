package com.huqiu.common.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {
    private Long userId;
    private Long merchantId;
    private String username;
    private String realName;
    private Integer role;   // 1 商家管理员 2 销售员

    public boolean isAdmin() {
        return role != null && role == 1;
    }
}