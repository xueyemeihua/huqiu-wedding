package com.huqiu.module.tailororder.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TailorOrderCreateDTO {
    @NotNull(message = "关联销售订单不能为空")
    private Long saleOrderId;
    private BigDecimal tailorCost;
    private LocalDateTime planFinishTime;
    private String remark;
    private Boolean draft; // 是否保存草稿
}