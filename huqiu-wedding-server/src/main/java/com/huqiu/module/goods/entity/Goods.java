package com.huqiu.module.goods.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("goods")
public class Goods {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private String skuCode;
    private String goodsName;
    private Long categoryId;
    private String size;
    private String color;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private BigDecimal rentPrice;
    private String goodsImg;
    private Integer status;   // 1正常 0下架
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}