package com.cmall.service;

import com.cmall.bean.OmsOrder;

public interface OrderService {
    String checkCode(String memberId,String tradeCode);

    String getTradeCode(String memberId);

    void saveOrder(OmsOrder omsOrder);
}
