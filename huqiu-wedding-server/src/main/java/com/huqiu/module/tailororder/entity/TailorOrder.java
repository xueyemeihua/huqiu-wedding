package com.huqiu.module.tailororder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("tailor_order")
public class TailorOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private String tailorNo;
    private Long saleOrderId;
    private String saleOrderNo;
    private Long saleUserId;
    private String customerName;
    private String customerPhone;
    private BigDecimal tailorCost;
    private LocalDateTime planFinishTime;
    private Integer status;   // 1待接单 2修改中 3已完工 4已交付 9已取消
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}