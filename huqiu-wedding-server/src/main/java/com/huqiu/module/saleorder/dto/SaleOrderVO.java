package com.huqiu.module.saleorder.dto;

import com.huqiu.module.saleorder.entity.SaleOrder;
import com.huqiu.module.saleorder.entity.SaleOrderItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SaleOrderVO extends SaleOrder {
    private List<SaleOrderItem> items;
}