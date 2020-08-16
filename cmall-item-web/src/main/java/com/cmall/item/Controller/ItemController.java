package com.cmall.item.Controller;


import com.alibaba.fastjson.JSON;
import com.cmall.bean.PmsProductSaleAttr;
import com.cmall.bean.PmsSkuInfo;
import com.cmall.bean.PmsSkuSaleAttrValue;
import com.cmall.service.SkuService;
import com.cmall.service.SpuService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;


    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap modelMap) {


        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId);
        //sku对象

        modelMap.put("skuInfo", pmsSkuInfo);

        //销售属性对象
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(), pmsSkuInfo.getId());
        modelMap.put("spuSaleAttrListCheckBySku", pmsProductSaleAttrs);

        //查询当前sku的spu的其他spu的集合的hash表
        Map<String, String> skuSaleAttrHash = new HashMap<>();
        List<PmsSkuInfo> pmsSkuInfos = skuService.getSkuSaleAttrValueListBySku(pmsSkuInfo.getProductId());
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String v = skuInfo.getId();
            String k = "";

            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                k += pmsSkuSaleAttrValue.getSaleAttrValueId() + "|"; //
            }
            skuSaleAttrHash.put(k, v);
        }

        //将sku的销售属性hash表放到页面
        String skuSaleAttrMathJsonStr = JSON.toJSONString(skuSaleAttrHash);
        modelMap.put("skuSaleAttrMathJsonStr", skuSaleAttrMathJsonStr);


        return "item";
    }


    @RequestMapping("index")
    public String index(ModelMap modelMap) {

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("循环数据" + i);
        }
        modelMap.put("list", list);

        modelMap.put("hello", "hello thymeleaf !");
        modelMap.put("check", "1");
        return "index";
    }
}
