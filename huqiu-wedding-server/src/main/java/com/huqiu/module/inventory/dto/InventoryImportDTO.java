package com.huqiu.module.inventory.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ColumnWidth(25)
public class InventoryImportDTO {

    @ExcelProperty("仓库名称")
    private String warehouseName;

    @ExcelProperty("商品款号")
    private String skuCode;

    @ExcelProperty("商品名称(仅展示不可改)")
    private String goodsName;

    @ExcelProperty("入库数量")
    private Integer importNum;

    @ExcelProperty("备注")
    private String remark;
}