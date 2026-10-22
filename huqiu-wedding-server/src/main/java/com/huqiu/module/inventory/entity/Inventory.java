package com.huqiu.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inventory")
public class Inventory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private Long warehouseId;
    private Long goodsId;
    private Integer totalStock;
    private Integer availableStock;
    private Integer occupyStock;
    @Version
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}