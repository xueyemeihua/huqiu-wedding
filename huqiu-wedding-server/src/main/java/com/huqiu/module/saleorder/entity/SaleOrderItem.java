package com.huqiu.module.saleorder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sale_order_item")
public class SaleOrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private Long orderId;
    private String orderNo;
    private Long warehouseId;
    private Long goodsId;
    private String skuCode;
    private BigDecimal price;
    private Integer num;
    private LocalDateTime createTime;
}