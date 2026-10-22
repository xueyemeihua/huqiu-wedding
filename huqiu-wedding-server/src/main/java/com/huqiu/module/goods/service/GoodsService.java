package com.huqiu.module.goods.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huqiu.common.context.UserContext;
import com.huqiu.common.exception.BusinessException;
import com.huqiu.common.result.PageResult;
import com.huqiu.module.goods.entity.Goods;
import com.huqiu.module.goods.entity.GoodsCategory;
import com.huqiu.module.goods.mapper.GoodsCategoryMapper;
import com.huqiu.module.goods.mapper.GoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodsService {

    private final GoodsMapper goodsMapper;
    private final GoodsCategoryMapper categoryMapper;

    public PageResult<Goods> page(int current, int size, String skuCode, String goodsName, Long categoryId) {
        Long merchantId = UserContext.require().getMerchantId();
        LambdaQueryWrapper<Goods> qw = new LambdaQueryWrapper<Goods>()
                .eq(Goods::getMerchantId, merchantId)
                .like(skuCode != null && !skuCode.isBlank(), Goods::getSkuCode, skuCode)
                .like(goodsName != null && !goodsName.isBlank(), Goods::getGoodsName, goodsName)
                .eq(categoryId != null, Goods::getCategoryId, categoryId)
                .orderByDesc(Goods::getId);
        return PageResult.of(goodsMapper.selectPage(new Page<>(current, size), qw));
    }

    public void save(Goods g) {
        Long merchantId = UserContext.require().getMerchantId();
        Goods exist = goodsMapper.selectOne(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getSkuCode, g.getSkuCode()));
        if (exist != null && !exist.getId().equals(g.getId())) {
            throw new BusinessException("款号已存在");
        }
        g.setMerchantId(merchantId);
        if (g.getStatus() == null) g.setStatus(1);
        if (g.getId() == null) {
            goodsMapper.insert(g);
        } else {
            goodsMapper.updateById(g);
        }
    }

    public void offline(Long id) {
        Long merchantId = UserContext.require().getMerchantId();
        Goods g = goodsMapper.selectById(id);
        if (g == null || !g.getMerchantId().equals(merchantId)) throw new BusinessException("商品不存在");
        g.setStatus(0);
        goodsMapper.updateById(g);
    }

    public List<GoodsCategory> categories() {
        Long merchantId = UserContext.require().getMerchantId();
        return categoryMapper.selectList(new LambdaQueryWrapper<GoodsCategory>()
                .eq(GoodsCategory::getMerchantId, merchantId)
                .orderByAsc(GoodsCategory::getSort));
    }

    public void saveCategory(GoodsCategory c) {
        c.setMerchantId(UserContext.require().getMerchantId());
        if (c.getId() == null) categoryMapper.insert(c);
        else categoryMapper.updateById(c);
    }
}