package com.huqiu.module.inventory.dto;

import com.huqiu.module.inventory.entity.Inventory;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InventoryVO extends Inventory {
    private String warehouseName;
    private String goodsName;
}