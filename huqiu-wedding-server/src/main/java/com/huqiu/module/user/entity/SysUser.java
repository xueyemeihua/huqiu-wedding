package com.huqiu.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private Integer role;    // 1商家管理员 2销售员
    private Integer status;  // 1启用 0禁用
    @TableField(fill = FieldFill.DEFAULT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.DEFAULT)
    private LocalDateTime updateTime;
}