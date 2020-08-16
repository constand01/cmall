package com.cmall.manage.server.mapper;

import com.cmall.bean.PmsSkuInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsSkuInfoMapper extends Mapper<PmsSkuInfo> {
    List<PmsSkuInfo> selecttSkuSaleAttrValueListBySku(String productId);
}
