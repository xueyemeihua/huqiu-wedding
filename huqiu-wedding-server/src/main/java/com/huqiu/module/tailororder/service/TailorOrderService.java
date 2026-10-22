package com.huqiu.module.tailororder.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huqiu.common.context.LoginUser;
import com.huqiu.common.context.UserContext;
import com.huqiu.common.exception.BusinessException;
import com.huqiu.common.result.PageResult;
import com.huqiu.common.util.OrderNoGenerator;
import com.huqiu.module.inventory.service.InventoryService;
import com.huqiu.module.saleorder.entity.SaleOrder;
import com.huqiu.module.saleorder.entity.SaleOrderItem;
import com.huqiu.module.saleorder.mapper.SaleOrderItemMapper;
import com.huqiu.module.saleorder.mapper.SaleOrderMapper;
import com.huqiu.module.tailororder.dto.TailorOrderCreateDTO;
import com.huqiu.module.tailororder.entity.TailorOrder;
import com.huqiu.module.tailororder.mapper.TailorOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TailorOrderService {
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PENDING = 1;
    private static final int STATUS_MODIFYING = 2;
    private static final int STATUS_FINISHED = 3;
    private static final int STATUS_DELIVERED = 4;
    private static final int STATUS_CANCELLED = 9;

    private final TailorOrderMapper tailorOrderMapper;
    private final SaleOrderMapper saleOrderMapper;
    private final SaleOrderItemMapper itemMapper;
    private final InventoryService inventoryService;
    private final OrderNoGenerator orderNoGenerator;

    @Transactional(rollbackFor = Exception.class)
    public Long create(TailorOrderCreateDTO dto) {
        LoginUser user = UserContext.require();
        SaleOrder so = saleOrderMapper.selectById(dto.getSaleOrderId());
        if (so == null || !so.getMerchantId().equals(user.getMerchantId())) {
            throw new BusinessException("销售订单不存在");
        }
        // 业务规则：只有售卖类型 (orderType == 1) 支持改衣
        if (so.getOrderType() != 1) {
            throw new BusinessException("仅售卖类型的销售订单支持新建改衣单");
        }
        if (so.getStatus() == 0 || so.getStatus() == 9) {
            throw new BusinessException("销售订单未生效或已取消，无法创建改衣单");
        }
        if (!user.isAdmin() && !so.getSaleUserId().equals(user.getUserId())) {
            throw new BusinessException("无法为他人的销售单创建改衣单");
        }

        boolean isDraft = Boolean.TRUE.equals(dto.getDraft());

        TailorOrder t = new TailorOrder();
        t.setMerchantId(user.getMerchantId());
        t.setTailorNo(orderNoGenerator.next("TO"));
        t.setSaleOrderId(so.getId());
        t.setSaleOrderNo(so.getOrderNo());
        t.setSaleUserId(user.getUserId());
        t.setCustomerName(so.getCustomerName());
        t.setCustomerPhone(so.getCustomerPhone());
        t.setTailorCost(dto.getTailorCost());
        t.setPlanFinishTime(dto.getPlanFinishTime());
        t.setRemark(dto.getRemark());
        t.setStatus(isDraft ? STATUS_DRAFT : STATUS_PENDING);
        tailorOrderMapper.insert(t);

//        if (!isDraft) {
//            lockInventory(so, t); // 非草稿直接锁定库存
//        }
        return t.getId();
    }

    /** 修改草稿 */
    @Transactional(rollbackFor = Exception.class)
    public void updateDraft(Long tailorId, TailorOrderCreateDTO dto) {
        LoginUser user = UserContext.require();
        TailorOrder t = tailorOrderMapper.selectById(tailorId);
        if (t == null || !t.getMerchantId().equals(user.getMerchantId())) {
            throw new BusinessException("改衣单不存在");
        }
        if (t.getStatus() != STATUS_DRAFT) {
            throw new BusinessException("仅草稿状态可修改");
        }
        t.setTailorCost(dto.getTailorCost());
        t.setPlanFinishTime(dto.getPlanFinishTime());
        t.setRemark(dto.getRemark());
        tailorOrderMapper.updateById(t);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long tailorId, Integer target) {
        LoginUser user = UserContext.require();
        TailorOrder t = tailorOrderMapper.selectById(tailorId);
        if (t == null || !t.getMerchantId().equals(user.getMerchantId())) {
            throw new BusinessException("改衣单不存在");
        }
        if (!user.isAdmin() && !t.getSaleUserId().equals(user.getUserId())) {
            throw new BusinessException("无权操作他人改衣单");
        }
        int cur = t.getStatus();
        if (cur == STATUS_CANCELLED || cur == STATUS_DELIVERED) {
            throw new BusinessException("改衣单已关闭");
        }

        boolean release = false;
        switch (target) {
            case STATUS_PENDING -> {
                if (cur != STATUS_DRAFT) throw new BusinessException("仅草稿状态可提交");
                SaleOrder so = saleOrderMapper.selectById(t.getSaleOrderId());
//                lockInventory(so, t); // 提交时锁定库存
            }
            case STATUS_MODIFYING -> {
                if (cur != STATUS_PENDING) throw new BusinessException("状态流转不合法");
            }
            case STATUS_FINISHED -> {
                if (cur != STATUS_MODIFYING) throw new BusinessException("状态流转不合法");
            }
            case STATUS_DELIVERED -> {
                if (cur != STATUS_FINISHED) throw new BusinessException("状态流转不合法");
                release = true;
            }
            case STATUS_CANCELLED -> {
                // 草稿取消不释放库存，生效后的取消才需要释放
                if (cur != STATUS_DRAFT) {
                    release = true;
                }
            }
            default -> throw new BusinessException("非法目标状态");
        }
        t.setStatus(target);
        tailorOrderMapper.updateById(t);

        if (release) {
            SaleOrder so = saleOrderMapper.selectById(t.getSaleOrderId());
//            releaseInventory(so, t);
        }
    }

//    // 抽取公共库存操作方法
//    private void lockInventory(SaleOrder so, TailorOrder t) {
//        List<SaleOrderItem> items = itemMapper.selectList(new LambdaQueryWrapper<SaleOrderItem>()
//                .eq(SaleOrderItem::getOrderId, so.getId()));
//        for (SaleOrderItem it : items) {
//            inventoryService.lockStock(it.getWarehouseId(), it.getGoodsId(), it.getNum(), t.getTailorNo());
//        }
//    }
//
//    private void releaseInventory(SaleOrder so, TailorOrder t) {
//        List<SaleOrderItem> items = itemMapper.selectList(new LambdaQueryWrapper<SaleOrderItem>()
//                .eq(SaleOrderItem::getOrderId, so.getId()));
//        for (SaleOrderItem it : items) {
//            inventoryService.releaseStock(it.getWarehouseId(), it.getGoodsId(), it.getNum(), t.getTailorNo());
//        }
//    }

    public PageResult<TailorOrder> page(int current, int size, Integer status, String saleOrderNo) {
        LoginUser user = UserContext.require();
        LambdaQueryWrapper<TailorOrder> qw = new LambdaQueryWrapper<TailorOrder>()
                .eq(TailorOrder::getMerchantId, user.getMerchantId())
                .eq(status != null, TailorOrder::getStatus, status)
                .like(saleOrderNo != null && !saleOrderNo.isBlank(), TailorOrder::getSaleOrderNo, saleOrderNo)
                .orderByDesc(TailorOrder::getId);
        if (!user.isAdmin()) qw.eq(TailorOrder::getSaleUserId, user.getUserId());
        return PageResult.of(tailorOrderMapper.selectPage(new Page<>(current, size), qw));
    }
}