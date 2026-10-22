package com.huqiu.module.saleorder.controller;

import com.huqiu.common.result.PageResult;
import com.huqiu.common.result.Result;
import com.huqiu.module.saleorder.dto.SaleOrderCreateDTO;
import com.huqiu.module.saleorder.dto.SaleOrderVO;
import com.huqiu.module.saleorder.entity.SaleOrder;
import com.huqiu.module.saleorder.service.SaleOrderService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sale-order")
@RequiredArgsConstructor
public class SaleOrderController {

    private final SaleOrderService saleOrderService;

    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody SaleOrderCreateDTO dto) {
        return Result.success(saleOrderService.create(dto));
    }

    @PutMapping("/update-status")
    public Result<Void> updateStatus(@RequestBody StatusDTO dto) {
        saleOrderService.updateStatus(dto.getOrderId(), dto.getStatus());
        return Result.success();
    }

    @PutMapping("/update-draft")
    public Result<Void> updateDraft(@RequestParam Long orderId, @Valid @RequestBody SaleOrderCreateDTO dto) {
        saleOrderService.updateDraft(orderId, dto);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<SaleOrder>> list(@RequestParam(defaultValue = "1") int current,
                                              @RequestParam(defaultValue = "20") int size,
                                              @RequestParam(required = false) String startTime,
                                              @RequestParam(required = false) String endTime,
                                              @RequestParam(required = false) Long saleUserId,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) String customerName,
                                              @RequestParam(required = false) String skuCode) {
        return Result.success(saleOrderService.page(current, size, startTime, endTime,
                saleUserId, status, customerName, skuCode));
    }

    @GetMapping("/{id}")
    public Result<SaleOrderVO> detail(@PathVariable Long id) {
        return Result.success(saleOrderService.detail(id));
    }

    @Data
    public static class StatusDTO {
        private Long orderId;
        private Integer status;
    }
}