package com.huqiu.module.tailororder.controller;

import com.huqiu.common.result.PageResult;
import com.huqiu.common.result.Result;
import com.huqiu.module.tailororder.dto.TailorOrderCreateDTO;
import com.huqiu.module.tailororder.entity.TailorOrder;
import com.huqiu.module.tailororder.service.TailorOrderService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tailor-order")
@RequiredArgsConstructor
public class TailorOrderController {

    private final TailorOrderService tailorOrderService;

    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody TailorOrderCreateDTO dto) {
        return Result.success(tailorOrderService.create(dto));
    }

    @PutMapping("/update-info")
    public Result<Void> updateInfo(@RequestParam Long tailorId, @Valid @RequestBody TailorOrderCreateDTO dto) {
        tailorOrderService.updateInfo(tailorId, dto);
        return Result.success();
    }

    @GetMapping("/list-by-sale/{saleOrderId}")
    public Result<List<TailorOrder>> listBySale(@PathVariable Long saleOrderId) {
        return Result.success(tailorOrderService.listBySaleOrderId(saleOrderId));
    }

    @PutMapping("/update-status")
    public Result<Void> updateStatus(@RequestBody StatusDTO dto) {
        tailorOrderService.updateStatus(dto.getTailorId(), dto.getStatus());
        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<TailorOrder>> list(@RequestParam(defaultValue = "1") int current,
                                                @RequestParam(defaultValue = "20") int size,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(required = false) String saleOrderNo) {
        return Result.success(tailorOrderService.page(current, size, status, saleOrderNo));
    }

    @Data
    public static class StatusDTO {
        private Long tailorId;
        private Integer status;
    }
}