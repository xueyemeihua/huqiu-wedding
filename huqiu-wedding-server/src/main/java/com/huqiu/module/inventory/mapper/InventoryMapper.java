package com.huqiu.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huqiu.module.inventory.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /** 乐观锁扣减可用库存 & 增加占用库存 */
    @Update("UPDATE inventory SET available_stock = available_stock - #{num}, " +
            "occupy_stock = occupy_stock + #{num}, version = version + 1 " +
            "WHERE id = #{id} AND version = #{version} AND available_stock >= #{num}")
    int lockStock(@Param("id") Long id, @Param("num") Integer num, @Param("version") Integer version);

    /** 释放占用库存 & 增加可用库存 */
    @Update("UPDATE inventory SET available_stock = available_stock + #{num}, " +
            "occupy_stock = occupy_stock - #{num}, version = version + 1 " +
            "WHERE id = #{id} AND occupy_stock >= #{num}")
    int releaseStock(@Param("id") Long id, @Param("num") Integer num);
}