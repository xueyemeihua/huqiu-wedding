package com.huqiu.module.saleorder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sale_order")
public class SaleOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private String orderNo;
    private Long saleUserId;
    private String customerName;
    private String customerPhone;
    private Integer orderType;      // 1售卖 2租赁
    private BigDecimal totalAmount;
    private Integer status;         // 0草稿 1已开单 2已取货 3已归还 4已完成 9已取消
    private LocalDateTime pickupTime;
    private LocalDateTime returnTime;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}