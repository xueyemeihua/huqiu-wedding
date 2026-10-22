package com.huqiu.module.goods.controller;

import com.huqiu.common.annotation.RequireRole;
import com.huqiu.common.result.PageResult;
import com.huqiu.common.result.Result;
import com.huqiu.module.goods.entity.Goods;
import com.huqiu.module.goods.entity.GoodsCategory;
import com.huqiu.module.goods.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    @GetMapping("/list")
    public Result<PageResult<Goods>> list(@RequestParam(defaultValue = "1") int current,
                                          @RequestParam(defaultValue = "20") int size,
                                          @RequestParam(required = false) String skuCode,
                                          @RequestParam(required = false) String goodsName,
                                          @RequestParam(required = false) Long categoryId) {
        return Result.success(goodsService.page(current, size, skuCode, goodsName, categoryId));
    }

    @PostMapping("/save")
    @RequireRole(1)
    public Result<Void> save(@RequestBody Goods goods) {
        goodsService.save(goods);
        return Result.success();
    }

    @PutMapping("/offline/{id}")
    @RequireRole(1)
    public Result<Void> offline(@PathVariable Long id) {
        goodsService.offline(id);
        return Result.success();
    }

    @GetMapping("/category/list")
    public Result<List<GoodsCategory>> categories() {
        return Result.success(goodsService.categories());
    }

    @PostMapping("/category/save")
    @RequireRole(1)
    public Result<Void> saveCategory(@RequestBody GoodsCategory category) {
        goodsService.saveCategory(category);
        return Result.success();
    }
}