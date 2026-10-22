package com.huqiu.module.warehouse.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huqiu.common.context.UserContext;
import com.huqiu.common.exception.BusinessException;
import com.huqiu.common.result.PageResult;
import com.huqiu.module.inventory.entity.Inventory;
import com.huqiu.module.inventory.mapper.InventoryMapper;
import com.huqiu.module.warehouse.entity.Warehouse;
import com.huqiu.module.warehouse.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseMapper warehouseMapper;
    private final InventoryMapper inventoryMapper;

    public PageResult<Warehouse> page(int current, int size, String keyword) {
        Long merchantId = UserContext.require().getMerchantId();
        LambdaQueryWrapper<Warehouse> qw = new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getMerchantId, merchantId)
                .like(keyword != null && !keyword.isBlank(), Warehouse::getWarehouseName, keyword)
                .orderByDesc(Warehouse::getId);
        return PageResult.of(warehouseMapper.selectPage(new Page<>(current, size), qw));
    }

    public void save(Warehouse w) {
        Long merchantId = UserContext.require().getMerchantId();
        w.setMerchantId(merchantId);
        if (w.getStatus() == null) w.setStatus(1);
        if (w.getId() == null) {
            warehouseMapper.insert(w);
        } else {
            Warehouse exist = warehouseMapper.selectById(w.getId());
            if (exist == null || !exist.getMerchantId().equals(merchantId)) {
                throw new BusinessException("仓库不存在");
            }
            warehouseMapper.updateById(w);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long merchantId = UserContext.require().getMerchantId();
        Warehouse w = warehouseMapper.selectById(id);
        if (w == null || !w.getMerchantId().equals(merchantId)) {
            throw new BusinessException("仓库不存在");
        }
        Long cnt = inventoryMapper.selectCount(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getWarehouseId, id)
                .gt(Inventory::getTotalStock, 0));
        if (cnt > 0) throw new BusinessException("该仓库下仍有库存，无法删除");
        warehouseMapper.deleteById(id);
    }
}