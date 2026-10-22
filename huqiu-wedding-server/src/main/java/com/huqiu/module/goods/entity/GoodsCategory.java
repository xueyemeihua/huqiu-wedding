package com.huqiu.module.goods.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("goods_category")
public class GoodsCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long merchantId;
    private String categoryName;
    private Integer sort;
    private LocalDateTime createTime;
}