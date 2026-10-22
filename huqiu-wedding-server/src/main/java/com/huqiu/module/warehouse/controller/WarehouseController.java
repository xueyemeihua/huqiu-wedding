package com.huqiu.module.warehouse.controller;

import com.huqiu.common.annotation.RequireRole;
import com.huqiu.common.result.PageResult;
import com.huqiu.common.result.Result;
import com.huqiu.module.warehouse.entity.Warehouse;
import com.huqiu.module.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping("/list")
    public Result<PageResult<Warehouse>> list(@RequestParam(defaultValue = "1") int current,
                                              @RequestParam(defaultValue = "20") int size,
                                              @RequestParam(required = false) String keyword) {
        return Result.success(warehouseService.page(current, size, keyword));
    }

    @PostMapping("/save")
    @RequireRole(1)
    public Result<Void> save(@RequestBody Warehouse warehouse) {
        warehouseService.save(warehouse);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireRole(1)
    public Result<Void> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return Result.success();
    }
}