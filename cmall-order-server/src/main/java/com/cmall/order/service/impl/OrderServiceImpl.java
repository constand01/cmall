package com.cmall.order.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.cmall.bean.OmsOrder;
import com.cmall.bean.OmsOrderItem;
import com.cmall.order.mapper.OmsOrderItemMapper;
import com.cmall.order.mapper.OmsOrderMapper;
import com.cmall.service.OrderService;
import com.cmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;







    @Override
    public String checkCode(String memberId,String tradeCode) {
        Jedis jedis=null;


       try {
           jedis=redisUtil.getJedis();
           String tradeKey = "user" + memberId + ":tradeCode";
           String tradeCodeFromCache = jedis.get(tradeKey);//使用lua脚本在发现key的同时将key删除，防止并发订单攻击
           //对比防重删令牌
           String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

           Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey), Collections.singletonList(tradeCode));




           if (eval!=null&&eval!=0) {
              // jedis.del(tradeKey);


               return "success";
           } else {
               return "fail";
           }
       }finally {
           jedis.close();
       }
    }

    @Override
    public String getTradeCode(String memberId) {

        Jedis jedis=redisUtil.getJedis();
        String tradeKey="user"+memberId+":tradeCode";
        String tradeCode= UUID.randomUUID().toString();

        jedis.setex(tradeKey,60*15,tradeCode);

        jedis.close();

        return tradeCode;
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {


        //保存订单表
        omsOrderMapper.insertSelective(omsOrder);
       String orderId= omsOrder.getId();
       //保存订单详情
        List<OmsOrderItem> omsOrderItems=omsOrder.getOmsOrderItems();

        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
        }
        //删除购物车数据
        //cartService.deCart();

    }


}
