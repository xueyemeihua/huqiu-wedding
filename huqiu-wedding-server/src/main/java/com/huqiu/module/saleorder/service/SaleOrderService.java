package com.huqiu.module.saleorder.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huqiu.common.context.LoginUser;
import com.huqiu.common.context.UserContext;
import com.huqiu.common.exception.BusinessException;
import com.huqiu.common.result.PageResult;
import com.huqiu.common.util.OrderNoGenerator;
import com.huqiu.module.goods.entity.Goods;
import com.huqiu.module.goods.mapper.GoodsMapper;
import com.huqiu.module.inventory.service.InventoryService;
import com.huqiu.module.saleorder.dto.SaleOrderCreateDTO;
import com.huqiu.module.saleorder.dto.SaleOrderVO;
import com.huqiu.module.saleorder.entity.SaleOrder;
import com.huqiu.module.saleorder.entity.SaleOrderItem;
import com.huqiu.module.saleorder.mapper.SaleOrderItemMapper;
import com.huqiu.module.saleorder.mapper.SaleOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleOrderService {

    // 订单状态
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_OPENED = 1;
    private static final int STATUS_PICKED = 2;
    private static final int STATUS_RETURNED = 3;
    private static final int STATUS_DONE = 4;
    private static final int STATUS_CANCELLED = 9;

    private final SaleOrderMapper orderMapper;
    private final SaleOrderItemMapper itemMapper;
    private final GoodsMapper goodsMapper;
    private final InventoryService inventoryService;
    private final OrderNoGenerator orderNoGenerator;

    /** 创建销售订单（草稿不扣库存，开单自动锁定库存） */
    @Transactional(rollbackFor = Exception.class)
    public Long create(SaleOrderCreateDTO dto) {
        LoginUser user = UserContext.require();
        boolean draft = Boolean.TRUE.equals(dto.getDraft());

        SaleOrder order = new SaleOrder();
        order.setMerchantId(user.getMerchantId());
        order.setOrderNo(orderNoGenerator.next("SO"));
        order.setSaleUserId(user.getUserId());
        order.setCustomerName(dto.getCustomerName());
        order.setCustomerPhone(dto.getCustomerPhone());
        order.setOrderType(dto.getOrderType());
        order.setStatus(draft ? STATUS_DRAFT : STATUS_OPENED);
        order.setRemark(dto.getRemark());
        order.setTotalAmount(BigDecimal.ZERO);
        orderMapper.insert(order);

        BigDecimal total = BigDecimal.ZERO;
        for (SaleOrderCreateDTO.Item it : dto.getItems()) {
            Goods g = goodsMapper.selectById(it.getGoodsId());
            if (g == null || !g.getMerchantId().equals(user.getMerchantId())) {
                throw new BusinessException("商品不存在：" + it.getGoodsId());
            }
            BigDecimal price = dto.getOrderType() == 2
                    ? (g.getRentPrice() == null ? BigDecimal.ZERO : g.getRentPrice())
                    : (g.getSalePrice() == null ? BigDecimal.ZERO : g.getSalePrice());

            SaleOrderItem item = new SaleOrderItem();
            item.setMerchantId(user.getMerchantId());
            item.setOrderId(order.getId());
            item.setOrderNo(order.getOrderNo());
            item.setWarehouseId(it.getWarehouseId());
            item.setGoodsId(it.getGoodsId());
            item.setSkuCode(g.getSkuCode());
            item.setPrice(price);
            item.setNum(it.getNum());
            itemMapper.insert(item);

            total = total.add(price.multiply(BigDecimal.valueOf(it.getNum())));

            if (!draft) {
                inventoryService.lockStock(it.getWarehouseId(), it.getGoodsId(), it.getNum(), order.getOrderNo());
            }
        }
        order.setTotalAmount(total);
        orderMapper.updateById(order);
        return order.getId();
    }

    /** 修改草稿 */
    @Transactional(rollbackFor = Exception.class)
    public void updateDraft(Long orderId, SaleOrderCreateDTO dto) {
        LoginUser user = UserContext.require();
        SaleOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getMerchantId().equals(user.getMerchantId())) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != STATUS_DRAFT) {
            throw new BusinessException("仅草稿状态可修改");
        }

        // 更新主表信息
        order.setCustomerName(dto.getCustomerName());
        order.setCustomerPhone(dto.getCustomerPhone());
        order.setOrderType(dto.getOrderType());
        order.setRemark(dto.getRemark());

        // 删除旧明细
        itemMapper.delete(new LambdaQueryWrapper<SaleOrderItem>().eq(SaleOrderItem::getOrderId, orderId));

        // 重新计算并插入明细
        BigDecimal total = BigDecimal.ZERO;
        for (SaleOrderCreateDTO.Item it : dto.getItems()) {
            Goods g = goodsMapper.selectById(it.getGoodsId());
            if (g == null) throw new BusinessException("商品不存在");
            BigDecimal price = dto.getOrderType() == 2
                    ? (g.getRentPrice() == null ? BigDecimal.ZERO : g.getRentPrice())
                    : (g.getSalePrice() == null ? BigDecimal.ZERO : g.getSalePrice());

            SaleOrderItem item = new SaleOrderItem();
            item.setMerchantId(user.getMerchantId());
            item.setOrderId(orderId);
            item.setOrderNo(order.getOrderNo());
            item.setWarehouseId(it.getWarehouseId());
            item.setGoodsId(it.getGoodsId());
            item.setSkuCode(g.getSkuCode());
            item.setPrice(price);
            item.setNum(it.getNum());
            itemMapper.insert(item);

            total = total.add(price.multiply(BigDecimal.valueOf(it.getNum())));
        }
        order.setTotalAmount(total);
        orderMapper.updateById(order);
    }

    /** 状态流转 */
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long orderId, Integer targetStatus) {
        LoginUser user = UserContext.require();
        SaleOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getMerchantId().equals(user.getMerchantId())) {
            throw new BusinessException("订单不存在");
        }
        if (!user.isAdmin() && !order.getSaleUserId().equals(user.getUserId())) {
            throw new BusinessException("无权操作他人订单");
        }
        int cur = order.getStatus();
        if (cur == STATUS_CANCELLED || cur == STATUS_DONE) {
            throw new BusinessException("订单已关闭");
        }

        List<SaleOrderItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<SaleOrderItem>().eq(SaleOrderItem::getOrderId, orderId));

        switch (targetStatus) {
            case STATUS_OPENED -> {
                if (cur != STATUS_DRAFT) throw new BusinessException("仅草稿状态可提交");
                // 草稿提交时，锁定库存
                for (SaleOrderItem it : items) {
                    inventoryService.lockStock(it.getWarehouseId(), it.getGoodsId(), it.getNum(), order.getOrderNo());
                }
            }
            case STATUS_PICKED -> {
                if (cur != STATUS_OPENED) throw new BusinessException("仅已开单状态可标记取货");
                order.setPickupTime(LocalDateTime.now());
            }
            case STATUS_RETURNED -> {
                if (cur != STATUS_PICKED) throw new BusinessException("仅已取货状态可标记归还");
                if (order.getOrderType() != 2) throw new BusinessException("非租赁订单无需归还");
                order.setReturnTime(LocalDateTime.now());
                releaseAll(items, order);
            }
            case STATUS_DONE -> {
                if (order.getOrderType() == 1 && cur != STATUS_OPENED && cur != STATUS_PICKED) {
                    throw new BusinessException("售卖单状态不合法");
                }
                if (order.getOrderType() == 2 && cur != STATUS_RETURNED) {
                    throw new BusinessException("租赁单请先归还");
                }

                // 如果订单是售卖类型，完成时执行真实出库
                if (order.getOrderType() == 1) {
                    for (SaleOrderItem it : items) {
                        // 扣减总库存和占用库存
                        inventoryService.deductStock(it.getWarehouseId(), it.getGoodsId(), it.getNum(), order.getOrderNo());
                    }
                }
            }
            case STATUS_CANCELLED -> {
                // 如果是从已开单取消，且未取货，释放库存
                if (cur == STATUS_OPENED && order.getPickupTime() == null) {
                    releaseAll(items, order);
                }
            }
            default -> throw new BusinessException("非法的目标状态");
        }
        order.setStatus(targetStatus);
        orderMapper.updateById(order);
    }

    private void releaseAll(List<SaleOrderItem> items, SaleOrder order) {
        for (SaleOrderItem it : items) {
            inventoryService.releaseStock(it.getWarehouseId(), it.getGoodsId(), it.getNum(), order.getOrderNo());
        }
    }

    public PageResult<SaleOrder> page(int current, int size, String startTime, String endTime,
                                      Long saleUserId, Integer status, String customerName, String skuCode) {
        LoginUser user = UserContext.require();
        LambdaQueryWrapper<SaleOrder> qw = new LambdaQueryWrapper<SaleOrder>()
                .eq(SaleOrder::getMerchantId, user.getMerchantId())
                .orderByDesc(SaleOrder::getId);
        // 数据隔离：销售员只看自己的
        if (!user.isAdmin()) {
            qw.eq(SaleOrder::getSaleUserId, user.getUserId());
        } else if (saleUserId != null) {
            qw.eq(SaleOrder::getSaleUserId, saleUserId);
        }
        if (StringUtils.hasText(startTime)) qw.ge(SaleOrder::getCreateTime, startTime);
        if (StringUtils.hasText(endTime)) qw.le(SaleOrder::getCreateTime, endTime);
        if (status != null) qw.eq(SaleOrder::getStatus, status);
        if (StringUtils.hasText(customerName)) qw.like(SaleOrder::getCustomerName, customerName);
        // skuCode 关联查询可以后续用 XML 扩展，这里简单通过 in 子查询
        if (StringUtils.hasText(skuCode)) {
            List<Long> orderIds = itemMapper.selectList(new LambdaQueryWrapper<SaleOrderItem>()
                            .eq(SaleOrderItem::getSkuCode, skuCode))
                    .stream().map(SaleOrderItem::getOrderId).toList();
            if (orderIds.isEmpty()) return new PageResult<>();
            qw.in(SaleOrder::getId, orderIds);
        }
        return PageResult.of(orderMapper.selectPage(new Page<>(current, size), qw));
    }

    public SaleOrderVO detail(Long orderId) {
        LoginUser user = UserContext.require();
        SaleOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getMerchantId().equals(user.getMerchantId())) {
            throw new BusinessException("订单不存在");
        }
        if (!user.isAdmin() && !order.getSaleUserId().equals(user.getUserId())) {
            throw new BusinessException("无权查看他人订单");
        }
        SaleOrderVO vo = new SaleOrderVO();
        org.springframework.beans.BeanUtils.copyProperties(order, vo);
        vo.setItems(itemMapper.selectList(new LambdaQueryWrapper<SaleOrderItem>()
                .eq(SaleOrderItem::getOrderId, orderId)));
        return vo;
    }
}