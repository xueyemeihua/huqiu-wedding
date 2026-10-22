package com.huqiu.module.report.controller;

import com.huqiu.common.annotation.RequireRole;
import com.huqiu.common.result.Result;
import com.huqiu.module.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@RequireRole(1)
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/business")
    public Result<Map<String, Object>> business() {
        return Result.success(reportService.business());
    }

    @GetMapping("/stock")
    public Result<Map<String, Object>> stock() {
        return Result.success(reportService.stock());
    }
}