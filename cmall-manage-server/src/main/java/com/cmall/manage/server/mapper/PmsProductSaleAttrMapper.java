package com.cmall.manage.server.mapper;

import com.cmall.bean.PmsProductSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsProductSaleAttrMapper extends Mapper<PmsProductSaleAttr> {
    List<PmsProductSaleAttr> selectspuSaleAttrListCheckBySku(@Param("productId") String productId, @Param("skuId") String skuId);

}
