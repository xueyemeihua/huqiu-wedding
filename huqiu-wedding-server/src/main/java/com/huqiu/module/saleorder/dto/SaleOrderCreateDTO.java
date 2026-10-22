package com.huqiu.module.saleorder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SaleOrderCreateDTO {
    @NotBlank(message = "客户姓名不能为空")
    private String customerName;
    @NotBlank(message = "客户电话不能为空")
    private String customerPhone;
    @NotNull(message = "租售类型不能为空")
    private Integer orderType;      // 1售卖 2租赁
    private String remark;
    /** true=保存草稿 false=直接开单 */
    private Boolean draft;

    @NotEmpty(message = "商品明细不能为空")
    private List<Item> items;

    @Data
    public static class Item {
        private Long warehouseId;
        private Long goodsId;
        private Integer num;
    }
}