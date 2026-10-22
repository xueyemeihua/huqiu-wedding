package com.huqiu.module.inventory.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huqiu.common.context.UserContext;
import com.huqiu.common.exception.BusinessException;
import com.huqiu.common.result.PageResult;
import com.huqiu.common.result.ResultCode;
import com.huqiu.module.goods.entity.Goods;
import com.huqiu.module.goods.mapper.GoodsMapper;
import com.huqiu.module.inventory.dto.InventoryImportDTO;
import com.huqiu.module.inventory.dto.InventoryVO;
import com.huqiu.module.inventory.entity.Inventory;
import com.huqiu.module.inventory.entity.InventoryLog;
import com.huqiu.module.inventory.mapper.InventoryLogMapper;
import com.huqiu.module.inventory.mapper.InventoryMapper;
import com.huqiu.module.warehouse.entity.Warehouse;
import com.huqiu.module.warehouse.mapper.WarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final int MAX_RETRY = 3;
    private final InventoryMapper inventoryMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final WarehouseMapper warehouseMapper;
    private final GoodsMapper goodsMapper;

    public PageResult<InventoryVO> page(int current, int size, Long warehouseId, Long goodsId) {
        Long merchantId = UserContext.require().getMerchantId();
        LambdaQueryWrapper<Inventory> qw = new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getMerchantId, merchantId)
                .eq(warehouseId != null, Inventory::getWarehouseId, warehouseId)
                .eq(goodsId != null, Inventory::getGoodsId, goodsId)
                .orderByDesc(Inventory::getId);

        IPage<Inventory> page = inventoryMapper.selectPage(new Page<>(current, size), qw);

        // 1. 转换为 VO
        IPage<InventoryVO> voPage = page.convert(inv -> {
            InventoryVO vo = new InventoryVO();
            BeanUtils.copyProperties(inv, vo);
            return vo;
        });

        // 2. 批量查询仓库和商品名称
        List<InventoryVO> voList = voPage.getRecords();
        if (!voList.isEmpty()) {
            List<Long> whIds = voList.stream().map(InventoryVO::getWarehouseId).distinct().toList();
            List<Long> gIds = voList.stream().map(InventoryVO::getGoodsId).distinct().toList();

            Map<Long, String> whMap = warehouseMapper.selectBatchIds(whIds).stream()
                    .collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));
            Map<Long, String> goodsMap = goodsMapper.selectBatchIds(gIds).stream()
                    .collect(Collectors.toMap(Goods::getId, Goods::getGoodsName));

            // 填充名称
            voList.forEach(vo -> {
                vo.setWarehouseName(whMap.getOrDefault(vo.getWarehouseId(), "未知仓库"));
                vo.setGoodsName(goodsMap.getOrDefault(vo.getGoodsId(), "未知商品"));
            });
        }
        return PageResult.of(voPage);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adjust(Long warehouseId, Long goodsId, int num, int changeType, String remark) {
        Long merchantId = UserContext.require().getMerchantId();
        Inventory inv = getOrCreate(merchantId, warehouseId, goodsId);
        inv.setTotalStock(inv.getTotalStock() + num);
        inv.setAvailableStock(inv.getAvailableStock() + num);
        if (inv.getAvailableStock() < 0) throw new BusinessException("可用库存不能小于 0");
        inventoryMapper.updateById(inv);
        writeLog(merchantId, warehouseId, goodsId, changeType, num, null, remark);
    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) {
        Long merchantId = UserContext.require().getMerchantId();

        // 1. 解析 Excel
        List<com.huqiu.module.inventory.dto.InventoryImportDTO> dataList = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), com.huqiu.module.inventory.dto.InventoryImportDTO.class, new ReadListener<com.huqiu.module.inventory.dto.InventoryImportDTO>() {
                @Override
                public void invoke(com.huqiu.module.inventory.dto.InventoryImportDTO data, AnalysisContext context) {
                    dataList.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                }
            }).sheet().doRead();
        } catch (Exception e) {
            throw new BusinessException("解析 Excel 失败，请检查文件格式");
        }

        if (dataList.isEmpty()) {
            throw new BusinessException("Excel 数据为空，无法导入");
        }

        // 2. 预加载仓库和商品，提升校验性能
        List<Warehouse> warehouses = warehouseMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getMerchantId, merchantId));
        Map<String, Long> warehouseMap = warehouses.stream()
                .collect(Collectors.toMap(Warehouse::getWarehouseName, Warehouse::getId, (k1, k2) -> k1));

        List<Goods> goodsList = goodsMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Goods>()
                .eq(Goods::getMerchantId, merchantId));
        Map<String, Long> goodsMap = goodsList.stream()
                .collect(Collectors.toMap(Goods::getSkuCode, Goods::getId, (k1, k2) -> k1));

        // 3. 逐行校验并执行入库
        for (int i = 0; i < dataList.size(); i++) {
            com.huqiu.module.inventory.dto.InventoryImportDTO row = dataList.get(i);
            int rowNum = i + 2; // Excel 行号 (表头占 1 行)

            if (row.getWarehouseName() == null || row.getSkuCode() == null || row.getImportNum() == null) {
                throw new BusinessException("第 " + rowNum + " 行：仓库名称、商品款号、入库数量不能为空");
            }
            if (row.getImportNum() <= 0) {
                throw new BusinessException("第 " + rowNum + " 行：入库数量必须大于 0");
            }

            Long warehouseId = warehouseMap.get(row.getWarehouseName().trim());
            if (warehouseId == null) {
                throw new BusinessException("第 " + rowNum + " 行：仓库 [" + row.getWarehouseName() + "] 不存在");
            }

            Long goodsId = goodsMap.get(row.getSkuCode().trim());
            if (goodsId == null) {
                throw new BusinessException("第 " + rowNum + " 行：商品款号 [" + row.getSkuCode() + "] 不存在");
            }

            // 调用已有的调整库存方法 (类型1为入库)
            // 注意：内部调用不走 AOP 代理，所以依附于当前方法的 @Transactional
            this.adjust(warehouseId, goodsId, row.getImportNum(), 1,
                    row.getRemark() == null ? "Excel批量导入" : "Excel导入:" + row.getRemark());
        }
    }

    public void lockStock(Long warehouseId, Long goodsId, int num, String orderNo) {
        Long merchantId = UserContext.require().getMerchantId();
        for (int i = 0; i < MAX_RETRY; i++) {
            Inventory inv = getOrCreate(merchantId, warehouseId, goodsId);
            if (inv.getAvailableStock() < num) throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            int rows = inventoryMapper.lockStock(inv.getId(), num, inv.getVersion());
            if (rows > 0) {
                writeLog(merchantId, warehouseId, goodsId, 3, -num, orderNo, "开单锁定");
                return;
            }
        }
        throw new BusinessException(ResultCode.STOCK_CONFLICT);
    }

    public void releaseStock(Long warehouseId, Long goodsId, int num, String orderNo) {
        Long merchantId = UserContext.require().getMerchantId();
        Inventory inv = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getMerchantId, merchantId).eq(Inventory::getWarehouseId, warehouseId).eq(Inventory::getGoodsId, goodsId));
        if (inv == null) throw new BusinessException("库存记录不存在");
        int rows = inventoryMapper.releaseStock(inv.getId(), num);
        if (rows == 0) throw new BusinessException(ResultCode.STOCK_CONFLICT);
        writeLog(merchantId, warehouseId, goodsId, 4, num, orderNo, "订单释放");
    }

    private Inventory getOrCreate(Long merchantId, Long warehouseId, Long goodsId) {
        Inventory inv = inventoryMapper.selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getMerchantId, merchantId).eq(Inventory::getWarehouseId, warehouseId).eq(Inventory::getGoodsId, goodsId));
        if (inv == null) {
            inv = new Inventory();
            inv.setMerchantId(merchantId);
            inv.setWarehouseId(warehouseId);
            inv.setGoodsId(goodsId);
            inv.setTotalStock(0);
            inv.setAvailableStock(0);
            inv.setOccupyStock(0);
            inv.setVersion(1);
            inventoryMapper.insert(inv);
        }
        return inv;
    }

    private void writeLog(Long merchantId, Long warehouseId, Long goodsId, int changeType, int changeNum, String orderNo, String remark) {
        InventoryLog log = new InventoryLog();
        log.setMerchantId(merchantId);
        log.setWarehouseId(warehouseId);
        log.setGoodsId(goodsId);
        log.setChangeType(changeType);
        log.setChangeNum(changeNum);
        log.setOrderNo(orderNo);
        log.setOperUserId(UserContext.require().getUserId());
        log.setRemark(remark);
        inventoryLogMapper.insert(log);
    }

    public PageResult<InventoryLog> pageLog(int current, int size, Long goodsId) {
        Long merchantId = UserContext.require().getMerchantId();
        LambdaQueryWrapper<InventoryLog> qw = new LambdaQueryWrapper<InventoryLog>()
                .eq(InventoryLog::getMerchantId, merchantId)
                .eq(goodsId != null, InventoryLog::getGoodsId, goodsId)
                .orderByDesc(InventoryLog::getId);
        return PageResult.of(inventoryLogMapper.selectPage(new Page<>(current, size), qw));
    }
}