package com.huqiu.module.report.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.huqiu.common.context.UserContext;
import com.huqiu.module.inventory.entity.Inventory;
import com.huqiu.module.inventory.mapper.InventoryMapper;
import com.huqiu.module.saleorder.entity.SaleOrder;
import com.huqiu.module.saleorder.mapper.SaleOrderMapper;
import com.huqiu.module.tailororder.entity.TailorOrder;
import com.huqiu.module.tailororder.mapper.TailorOrderMapper;
import com.huqiu.module.user.entity.SysUser;
import com.huqiu.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SaleOrderMapper saleOrderMapper;
    private final TailorOrderMapper tailorOrderMapper;
    private final InventoryMapper inventoryMapper;
    private final SysUserMapper userMapper;

    public Map<String, Object> business() {
        Long merchantId = UserContext.require().getMerchantId();
        Map<String, Object> result = new LinkedHashMap<>();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        result.put("todayOrderCount", countOrders(merchantId, todayStart));
        result.put("weekOrderCount", countOrders(merchantId, weekStart));
        result.put("monthOrderCount", countOrders(merchantId, monthStart));

        result.put("todayAmount", sumAmount(merchantId, todayStart));
        result.put("monthAmount", sumAmount(merchantId, monthStart));

        // 销售员业绩排行
        List<SaleOrder> all = saleOrderMapper.selectList(new LambdaQueryWrapper<SaleOrder>()
                .eq(SaleOrder::getMerchantId, merchantId)
                .ge(SaleOrder::getCreateTime, monthStart)
                .notIn(SaleOrder::getStatus, 0, 9));
        Map<Long, BigDecimal> rank = new HashMap<>();
        all.forEach(o -> rank.merge(o.getSaleUserId(), o.getTotalAmount(), BigDecimal::add));
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getMerchantId, merchantId).eq(SysUser::getRole, 2));
        Map<Long, String> nameMap = users.stream().collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));
        List<Map<String, Object>> ranking = rank.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("userId", e.getKey());
                    m.put("userName", nameMap.getOrDefault(e.getKey(), "未知"));
                    m.put("amount", e.getValue());
                    return m;
                }).toList();
        result.put("salesRanking", ranking);

        // 待取货 / 待归还 / 待改衣
        result.put("pendingPickup", saleOrderMapper.selectCount(new LambdaQueryWrapper<SaleOrder>()
                .eq(SaleOrder::getMerchantId, merchantId).eq(SaleOrder::getStatus, 1)));
        result.put("pendingReturn", saleOrderMapper.selectCount(new LambdaQueryWrapper<SaleOrder>()
                .eq(SaleOrder::getMerchantId, merchantId).eq(SaleOrder::getStatus, 2)));
        result.put("pendingTailor", tailorOrderMapper.selectCount(new LambdaQueryWrapper<TailorOrder>()
                .eq(TailorOrder::getMerchantId, merchantId).in(TailorOrder::getStatus, 1, 2)));

        // 租售占比
        long rent = all.stream().filter(o -> o.getOrderType() == 2).count();
        long sale = all.stream().filter(o -> o.getOrderType() == 1).count();
        result.put("rentCount", rent);
        result.put("saleCount", sale);

        return result;
    }

    private long countOrders(Long merchantId, LocalDateTime from) {
        return saleOrderMapper.selectCount(new LambdaQueryWrapper<SaleOrder>()
                .eq(SaleOrder::getMerchantId, merchantId)
                .ge(SaleOrder::getCreateTime, from)
                .notIn(SaleOrder::getStatus, 0, 9));
    }

    private BigDecimal sumAmount(Long merchantId, LocalDateTime from) {
        List<SaleOrder> list = saleOrderMapper.selectList(new LambdaQueryWrapper<SaleOrder>()
                .eq(SaleOrder::getMerchantId, merchantId)
                .ge(SaleOrder::getCreateTime, from)
                .notIn(SaleOrder::getStatus, 0, 9));
        return list.stream().map(SaleOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<String, Object> stock() {
        Long merchantId = UserContext.require().getMerchantId();
        List<Inventory> list = inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getMerchantId, merchantId));
        int total = list.stream().mapToInt(Inventory::getTotalStock).sum();
        int available = list.stream().mapToInt(Inventory::getAvailableStock).sum();
        int occupy = list.stream().mapToInt(Inventory::getOccupyStock).sum();
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("totalStock", total);
        r.put("availableStock", available);
        r.put("occupyStock", occupy);
        r.put("items", list);
        return r;
    }
}