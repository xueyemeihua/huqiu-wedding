package com.huqiu.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inventory_log")
public class InventoryLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private Long warehouseId;
    private Long goodsId;
    private Integer changeType;  // 1入库 2出库 3订单占用 4订单释放 5盘点调整
    private Integer changeNum;
    private String orderNo;
    private Long operUserId;
    private String remark;
    private LocalDateTime createTime;
}