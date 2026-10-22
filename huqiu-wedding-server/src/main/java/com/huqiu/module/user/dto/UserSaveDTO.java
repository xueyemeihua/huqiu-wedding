package com.huqiu.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserSaveDTO {
    private Long id;
    @NotBlank(message = "姓名不能为空")
    private String realName;
    @NotBlank(message = "手机号不能为空")
    private String phone;
    @NotBlank(message = "登录账号不能为空")
    private String username;
    private String password;  // 新增必填，编辑时为空表示不修改
    @NotNull(message = "角色不能为空")
    private Integer role;     // 1管理员 2销售员
    private Integer status;
}