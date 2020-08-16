package com.cmall.searc.controller;


import annotations.LoginRequired;
import com.cmall.bean.*;
import com.cmall.service.AttrService;
import com.cmall.service.SearchService;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;


    @RequestMapping("list.html")
    public String list(PmsSearchParms pmsSearchParms, ModelMap modelMap) {
        //调用 搜索服务，返回搜索结果
        List<PmsSearchSkuInfo> searchSkuInfos = searchService.list(pmsSearchParms);
        modelMap.put("skuLsInfoList", searchSkuInfos);


        Set<String> valueIdSet = new HashSet<>();

        for (PmsSearchSkuInfo searchSkuInfo : searchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValues = searchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue skuAttrValue : skuAttrValues) {
                String valueId = skuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }

        //根据valueid将属性列表查询出来
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdSet);
        modelMap.put("attrList", pmsBaseAttrInfos);

        //对平台属性集合进一步处理，去掉当前条件中value所在的属性

        String[] delValueIds = pmsSearchParms.getValueId();

        if (delValueIds != null) {
            //面包屑
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();


            for (String delValueId : delValueIds) {
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();//平台属性集合
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setUrlParam(geturlParamForCrumb(pmsSearchParms, delValueId));
                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();

                    List<PmsBaseAttrValue> attrInfos = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue attrInfo : attrInfos) {
                        String valueId = attrInfo.getId();



                        if (delValueId.equals(valueId)) {
                            //查找面包屑的属性值
                            pmsSearchCrumb.setValueName(attrInfo.getValueName());
                            //删除该属性所在的属性组
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbs.add(pmsSearchCrumb);


            }
            modelMap.put("attrValueSelectedList", pmsSearchCrumbs);
        }


        String urlParam = geturlParam(pmsSearchParms);

        modelMap.put("urlParam", urlParam);
        String keyword = pmsSearchParms.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            modelMap.put("keyword", keyword);
        }

        return "list";
    }


    public String geturlParamForCrumb(PmsSearchParms pmsSearchParms, String delValueID) {

        String catalog3Id = pmsSearchParms.getCatalog3Id();
        String keyword = pmsSearchParms.getKeyword();
        String[] pmsSkuAttrValueList = pmsSearchParms.getValueId();

        String urlParam = "";
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }

        if (pmsSkuAttrValueList != null) {


            for (String pmsSkuAttrValue : pmsSkuAttrValueList) {
                if (!pmsSkuAttrValue.equals(delValueID)) {
                    urlParam = urlParam + "&valueId=" + pmsSkuAttrValue;
                }
            }
        }


        return urlParam;
    }


    public String geturlParam(PmsSearchParms pmsSearchParms) {

        String catalog3Id = pmsSearchParms.getCatalog3Id();
        String keyword = pmsSearchParms.getKeyword();
        String[] pmsSkuAttrValueList = pmsSearchParms.getValueId();

        String urlParam = "";
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }

        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }

        if (pmsSkuAttrValueList != null) {


            for (String pmsSkuAttrValue : pmsSkuAttrValueList) {
                urlParam = urlParam + "&valueId=" + pmsSkuAttrValue;
            }
        }


        return urlParam;
    }


    @RequestMapping("index")
    @LoginRequired(loginSeccess = false)
    public String index() {
        return "index";
    }
}
