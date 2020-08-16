package com.cmall.manage.web.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.cmall.bean.PmsBaseCatalog1;
import com.cmall.bean.PmsBaseCatalog2;
import com.cmall.bean.PmsBaseCatalog3;
import com.cmall.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class CatalogController {

    @Reference
    CatalogService catalogService;


    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {
        List<PmsBaseCatalog2> catalog2 = catalogService.getCatalog2(catalog1Id);
        return catalog2;

    }


    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1() {
        List<PmsBaseCatalog1> catalog1 = catalogService.getCatalog1();
        return catalog1;

    }


    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {
        List<PmsBaseCatalog3> catalog3 = catalogService.getCatalog3(catalog2Id);
        return catalog3;

    }
}
