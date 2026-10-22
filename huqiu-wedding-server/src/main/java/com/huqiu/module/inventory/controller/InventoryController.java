package com.huqiu.module.inventory.controller;

import com.alibaba.excel.EasyExcel;
import com.huqiu.common.annotation.RequireRole;
import com.huqiu.common.result.PageResult;
import com.huqiu.common.result.Result;
import com.huqiu.module.inventory.dto.InventoryImportDTO;
import com.huqiu.module.inventory.dto.InventoryVO;
import com.huqiu.module.inventory.entity.Inventory;
import com.huqiu.module.inventory.entity.InventoryLog;
import com.huqiu.module.inventory.service.InventoryService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.util.ArrayList;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/list")
    public Result<PageResult<InventoryVO>> list(@RequestParam(defaultValue = "1") int current,
                                                @RequestParam(defaultValue = "20") int size,
                                                @RequestParam(required = false) Long warehouseId,
                                                @RequestParam(required = false) Long goodsId) {
        return Result.success(inventoryService.page(current, size, warehouseId, goodsId));
    }

    @PostMapping("/stock-change")
    @RequireRole(1)
    public Result<Void> change(@RequestBody StockChangeDTO dto) {
        inventoryService.adjust(dto.getWarehouseId(), dto.getGoodsId(),
                dto.getNum(), dto.getChangeType(), dto.getRemark());
        return Result.success();
    }

    @GetMapping("/log")
    public Result<PageResult<InventoryLog>> log(@RequestParam(defaultValue = "1") int current,
                                                @RequestParam(defaultValue = "20") int size,
                                                @RequestParam(required = false) Long goodsId) {
        return Result.success(inventoryService.pageLog(current, size, goodsId));
    }

    /**
     * 下载 Excel 导入模板
     */
    @GetMapping("/import-template")
    public void downloadTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("库存入库导入模板", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            // 写入表头，空数据
            EasyExcel.write(response.getOutputStream(), InventoryImportDTO.class)
                    .sheet("入库明细").doWrite(new ArrayList<>());
        } catch (Exception e) {
            throw new com.huqiu.common.exception.BusinessException("模板下载失败");
        }
    }

    /**
     * Excel 批量导入入库
     */
    @PostMapping("/import")
    public Result<Void> importExcel(@RequestParam("file") MultipartFile file) {
        inventoryService.importExcel(file);
        return Result.success();
    }

    @Data
    public static class StockChangeDTO {
        private Long warehouseId;
        private Long goodsId;
        private Integer num;
        private Integer changeType;  // 1入库 2出库 5盘点调整
        private String remark;
    }
}