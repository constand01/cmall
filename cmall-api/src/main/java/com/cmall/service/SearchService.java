package com.cmall.service;

import com.cmall.bean.PmsSearchParms;
import com.cmall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParms pmsSearchParms);
}
